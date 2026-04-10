# Documento de Requisitos: Sistema de Adoção - Centro de Zoonoses

## 1. Introdução

### 1.1 Propósito

Este documento tem como objetivo descrever os requisitos funcionais e não funcionais do sistema de adoção de animais para o Centro de Zoonoses, servindo como base para desenvolvimento, validação e manutenção do sistema.

### 1.2 Escopo

O sistema permitirá:

- Cadastro e gerenciamento de animais disponíveis para adoção
- Agendamento de visitas por adotantes
- Controle de horários disponíveis
- Acompanhamento das visitas por funcionários
- Registro do resultado do processo de adoção

### 1.3 Definições

- **Adotante:** Usuário que deseja adotar um animal
- **Funcionário:** Responsável por acompanhar visitas
- **Administrador:** Usuário com acesso total ao sistema
- **Agendamento (Appointment):** Registro de uma visita
- **TimeSlot:** Horário disponível para agendamento

---

## 2. Visão Geral do Sistema

O sistema é uma aplicação web composta por:

- **Backend:** Java com Spring Boot
- **Frontend:** React
- **Banco de Dados:** MySQL

O foco principal é organizar e automatizar o processo de adoção de animais através do agendamento de visitas.

---

## 3. Requisitos Funcionais

### 3.1 Gestão de Usuários

- **RF01 - Cadastro de Usuário:** O sistema deve permitir o cadastro de usuários adotantes.
- **RF02 - Autenticação:** O sistema deve permitir login com email e senha.
- **RF03 - Controle de Acesso:** O sistema deve diferenciar permissões entre Administrador, Funcionário e Adotante.

### 3.2 Gestão de Pets

- **RF04 - Cadastro de Pet:** O sistema deve permitir que administradores cadastrem novos pets.
- **RF05 - Visualização de Pets:** O sistema deve listar todos os pets disponíveis para adoção.
- **RF06 - Detalhamento de Pet:** O sistema deve exibir informações detalhadas do pet.

### 3.3 Agendamento de Visitas

- **RF07 - Visualização de Horários:** O sistema deve exibir horários disponíveis para agendamento.
- **RF08 - Agendamento de Visita:** O adotante deve poder agendar uma visita para um pet.
- **RF09 - Confirmação de Agendamento:** O sistema deve confirmar o agendamento via notificação.
- **RF10 - Cancelamento de Agendamento:** O usuário deve poder cancelar um agendamento.

### 3.4 Gestão de Atendimentos

- **RF11 - Visualização de Agenda (Admin):** O administrador deve visualizar todos os agendamentos.
- **RF12 - Atribuição de Funcionário:** O administrador deve atribuir um funcionário a um agendamento.
- **RF13 - Visualização de Agenda (Funcionário):** O funcionário deve visualizar apenas seus atendimentos.

### 3.5 Processo de Adoção

- **RF14 - Registro de Resultado:** O sistema deve permitir registrar o resultado da visita (Aprovado, Rejeitado, Pendente).
- **RF15 - Histórico de Adoções:** O administrador deve visualizar o histórico de visitas e adoções.

---

## 4. Requisitos Não Funcionais

### 4.1 Desempenho

- **RNF01:** O sistema deve suportar múltiplos usuários simultâneos.
- **RNF02:** As operações devem responder em tempo aceitável (< 2 segundos).

### 4.2 Segurança

- **RNF03:** O sistema deve utilizar autenticação segura.
- **RNF04:** O acesso deve ser controlado por perfis de usuário.

### 4.3 Usabilidade

- **RNF05:** A interface deve ser
