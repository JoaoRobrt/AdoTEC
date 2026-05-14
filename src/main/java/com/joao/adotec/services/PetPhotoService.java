package com.joao.adotec.services;

import com.joao.adotec.exceptions.domain.BusinessException;
import com.joao.adotec.exceptions.domain.ResourceNotFoundException;
import com.joao.adotec.models.Pet;
import com.joao.adotec.models.PetPhoto;
import com.joao.adotec.repositories.PetPhotoRepository;
import com.joao.adotec.repositories.PetRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class PetPhotoService {

    private final PetPhotoRepository petPhotoRepository;
    private final PetRepository petRepository;
    private final CloudinaryService cloudinaryService;

    private static final long MAX_FILE_SIZE = 5 * 1024 * 1024; // 5MB
    private static final List<String> ALLOWED_CONTENT_TYPES = List.of("image/jpeg", "image/png", "image/webp");
    private static final int MAX_PHOTOS_PER_PET = 10;

    @Transactional
    public PetPhoto uploadPhoto(Long petId, MultipartFile file) {
        Pet pet = getActivePet(petId);

        validateFile(file);

        if (petPhotoRepository.countByPet(pet) >= MAX_PHOTOS_PER_PET) {
            throw new BusinessException("Maximum limit of " + MAX_PHOTOS_PER_PET + " photos per pet reached.");
        }

        Map<String, Object> uploadResult = cloudinaryService.uploadPhoto(file);
        
        String url = (String) uploadResult.get("secure_url");
        String publicId = (String) uploadResult.get("public_id");
        
        boolean isPrimary = petPhotoRepository.countByPet(pet) == 0; // First photo is primary by default

        PetPhoto petPhoto = new PetPhoto(pet, url, publicId, isPrimary);
        return petPhotoRepository.save(petPhoto);
    }

    @Transactional
    public void deletePhoto(Long petId, Long photoId) {
        Pet pet = getActivePet(petId);
        
        PetPhoto petPhoto = petPhotoRepository.findById(photoId)
                .orElseThrow(() -> new ResourceNotFoundException("PetPhoto", photoId));

        if (!petPhoto.getPet().getPetId().equals(pet.getPetId())) {
            throw new ResourceNotFoundException("PetPhoto " + photoId + " not found for Pet " + petId);
        }

        boolean wasPrimary = petPhoto.getIsPrimary();

        cloudinaryService.deletePhoto(petPhoto.getPublicId());
        petPhotoRepository.delete(petPhoto);
        
        if (wasPrimary) {
            java.util.List<PetPhoto> remainingPhotos = petPhotoRepository.findByPetOrderByCreatedAtAsc(pet);
            if (!remainingPhotos.isEmpty()) {
                PetPhoto oldestRemaining = remainingPhotos.get(0);
                oldestRemaining.setIsPrimary(true);
                petPhotoRepository.save(oldestRemaining);
            }
        }
    }

    @Transactional
    public PetPhoto setPrimaryPhoto(Long petId, Long photoId) {
        Pet pet = getActivePet(petId);
        
        PetPhoto newPrimary = petPhotoRepository.findById(photoId)
                .orElseThrow(() -> new ResourceNotFoundException("PetPhoto", photoId));

        if (!newPrimary.getPet().getPetId().equals(pet.getPetId())) {
            throw new ResourceNotFoundException("PetPhoto " + photoId + " not found for Pet " + petId);
        }

        List<PetPhoto> photos = petPhotoRepository.findByPetOrderByCreatedAtAsc(pet);
        for (PetPhoto photo : photos) {
            if (photo.getIsPrimary()) {
                photo.setIsPrimary(false);
                petPhotoRepository.save(photo);
            }
        }

        newPrimary.setIsPrimary(true);
        return petPhotoRepository.save(newPrimary);
    }

    @Transactional(readOnly = true)
    public List<PetPhoto> getPhotosByPet(Long petId) {
        Pet pet = getActivePet(petId);
        return petPhotoRepository.findByPetOrderByCreatedAtAsc(pet);
    }

    private Pet getActivePet(Long petId) {
        Pet pet = petRepository.findById(petId)
                .orElseThrow(() -> new ResourceNotFoundException("Pet", petId));
        
        if (!pet.getIsActive()) {
            throw new ResourceNotFoundException("Pet (Inactive)", petId);
        }
        return pet;
    }

    private void validateFile(MultipartFile file) {
        if (file == null || file.isEmpty()) {
            throw new BusinessException("File is empty or missing.");
        }

        if (file.getSize() > MAX_FILE_SIZE) {
            throw new BusinessException("File size exceeds the maximum limit of 5MB.");
        }

        if (file.getContentType() == null || !ALLOWED_CONTENT_TYPES.contains(file.getContentType())) {
            throw new BusinessException("Invalid file type. Only JPEG, PNG, and WebP are allowed.");
        }

        try {
            byte[] bytes = file.getBytes();
            if (bytes.length < 12) {
                throw new BusinessException("Invalid file format.");
            }

            boolean isJpeg = bytes[0] == (byte) 0xFF && bytes[1] == (byte) 0xD8 && bytes[2] == (byte) 0xFF;
            boolean isPng = bytes[0] == (byte) 0x89 && bytes[1] == (byte) 0x50 && bytes[2] == (byte) 0x4E && bytes[3] == (byte) 0x47;
            boolean isWebp = bytes[0] == 'R' && bytes[1] == 'I' && bytes[2] == 'F' && bytes[3] == 'F' &&
                             bytes[8] == 'W' && bytes[9] == 'E' && bytes[10] == 'B' && bytes[11] == 'P';

            if (!isJpeg && !isPng && !isWebp) {
                throw new BusinessException("Invalid file content. The file is not a valid image format (magic bytes mismatch).");
            }
        } catch (java.io.IOException e) {
            throw new BusinessException("Failed to read file content.");
        }
    }
}
