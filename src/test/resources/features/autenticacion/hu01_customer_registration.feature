# language: en
@hu01 @registration
Feature: Customer registration
  As an unregistered visitor
  I want to provide my personal information
  So that I can create my profile in the digital bank

  @critical
  Scenario: Register a customer with valid information
    Given I am an unregistered visitor
    And I provide valid personal information
    When I request to register
    Then my customer profile should be created
    And I should see the success message "El usuario ha sido registrado con éxito."

  Scenario: Register with an already registered email address
    Given my email address is already registered
    When I request to register using the same email address
    Then my registration should be rejected
    And I should see the error message "El correo ingresado ya está registrado."
