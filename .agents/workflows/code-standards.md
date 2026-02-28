---
description: Guidelines for code generation, modification, and addition
---

# Code Generation & Modification Standards

Follow this workflow anytime you are generating fresh code, modifying existing components, or adding new features to the project.

These guidelines ensure all code remains clean, secure, and maintainable.

## Core Principles

1. **Modern & Secure**: Always use the latest, most secure programming features and libraries available to the project's stack. Avoid deprecated APIs and legacy patterns.
2. **Readable & Maintainable**: Write clean, self-documenting code. Favor clear variable and method names over abbreviations.
3. **Scalable Architecture**: Design code with future growth in mind. Decouple components where appropriate and adhere to SOLID principles.

## Commenting Rules

- **DO** comment on complex algorithms, non-obvious business logic, or mathematical formulas to explain *why* something is done.
- **DO NOT** comment on every single line or explain obvious syntax (e.g., avoid `// increments x by 1` above `x++`). The code should be explicitly readable on its own.

## Steps for Code Tasks

1. **Analyze existing patterns**: Before writing new code, review the surrounding architecture to ensure consistency with the established project style.
2. **Draft the logic**: Ensure the structure is scalable (e.g., using interfaces, dependency injection, or proper class encapsulation as needed).
3. **Implement**: Write the code strictly following the Core Principles.
4. **Review & Refine**: Self-audit the new code to ensure it meets readability standards and does not introduce security risks or memory leaks.
