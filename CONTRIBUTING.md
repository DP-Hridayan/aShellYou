# Contributing to aShell You

Thank you for your interest in contributing to aShell You! We appreciate your time and effort in helping us improve this project. This guide will help you get started with contributing.

---

## Code of Conduct

Please read and follow our [Code of Conduct](CODE_OF_CONDUCT.md). We are committed to providing a welcoming and inclusive environment for all contributors. By participating, you agree to abide by this code.

---

## How to Contribute

There are many ways to contribute to this project:

- **Report Bugs**: Open an issue to report a bug.
- **Suggest Features**: Suggest new features or improvements.
- **Write Code**: Submit a pull request to fix bugs or add features.
- **Improve Documentation**: Help us improve the project's documentation.
- **Test the App**: Test the app and report any issues.

---

## Setting Up the Project

To get started with contributing, follow these steps:

### Prerequisites

- **Java Development Kit (JDK)**: Ensure you have JDK 11 or higher installed.
- **Android Studio**: Install the latest version of Android Studio.
- **Git**: Install Git to clone and manage the repository.

### Steps

1. **Fork the Repository**:
   - Click the "Fork" button on the top right of the repository page to create your own copy.

2. **Clone the Repository**:
   ```bash
   git clone https://github.com/DP-Hridayan/aShellYou.git
   cd aShellYou
   ```

3. **Open the Project in Android Studio**:
   - Open Android Studio and select "Open an Existing Project."
   - Navigate to the cloned repository and open it.

4. **Install Dependencies**:
   - Sync the project with Gradle to download all dependencies.

5. **Run the App**:
   - Connect an Android device or start an emulator.
   - Click the "Run" button in Android Studio to build and run the app.

---

## Reporting Bugs

If you find a bug, please open an issue and include the following information:

- **Description**: A clear and concise description of the bug.
- **Steps to Reproduce**: Detailed steps to reproduce the issue.
- **Expected Behavior**: What you expected to happen.
- **Actual Behavior**: What actually happened.
- **Screenshots**: If applicable, add screenshots to help explain the issue.
- **Device Information**:
  - Device model
  - Android version
  - App version

**Example**:

### Bug Report

**Description**: The app crashes when clicking the "Save" button.

**Steps to Reproduce**:
1. Open the app.
2. Navigate to the "Settings" screen.
3. Click the "Save" button.

**Expected Behavior**: The settings should be saved without any issues.

**Actual Behavior**: The app crashes immediately after clicking the "Save" button.

**Device Information**:
- Device: Pixel 5
- Android Version: 12
- App Version: 1.0.0

## Suggesting Features

If you have an idea for a new feature or improvement, please open an issue and include the following information:

- **Description**: A clear and concise description of the feature.
- **Problem**: Explain the problem this feature will solve.
- **Proposed Solution**: Describe your proposed solution.
- **Alternatives**: List any alternative solutions you considered.

**Example**:

### Feature Request

**Description**: Add a dark mode to the app.

**Problem**: The app's current light theme is not suitable for low-light environments.

**Proposed Solution**: Implement a dark mode that can be toggled in the settings.

**Alternatives**: Use system-wide dark mode settings.

---

## Writing Code

If you'd like to contribute code, follow these steps:

### 1. Create a Branch
   - Create a new branch for your feature or bug fix:
     ```bash
     git checkout -b feature/your-feature-name
     ```

### 2. Make Your Changes
   - Write your code and ensure it follows the project's coding standards (see below).

### 3. Test Your Changes
   - Run the app and ensure your changes work as expected.
   - Write unit tests if applicable.

### 4. Commit Your Changes
   - Write clear and concise commit messages:
     ```bash
     git commit -m "Add dark mode feature"
     ```

### 5. Push Your Changes
   - Push your branch to your forked repository:
     ```bash
     git push origin feature/your-feature-name
     ```

### 6. Open a Pull Request
   - Go to the original repository and click "New Pull Request."
   - Provide a clear title and description for your pull request.
   - Reference any related issues (e.g., "Fixes #123").

---

## Coding Standards

To maintain consistency, please follow these coding standards:

- **Java**:
  - Use 4 spaces for indentation.
  - Follow the [Google Java Style Guide](https://google.github.io/styleguide/javaguide.html).
- **XML**:
  - Use 2 spaces for indentation.
  - Use descriptive IDs and names for resources.
- **Commit Messages**:
  - Use the present tense (e.g., "Add feature" instead of "Added feature").
  - Keep the first line under 50 characters.
  - Provide a detailed description if necessary.

---

## Testing

Before submitting your changes, ensure the app works as expected:

- **Unit Tests**: Run all unit tests in Android Studio.
- **UI Tests**: Run UI tests to ensure no regressions.
- **Manual Testing**: Test the app on different devices and Android versions.

---

## Code Review Process

1. **Pull Request Submission**:
   - Open a pull request with a clear description of your changes.
   - Ensure your code passes all tests and follows the coding standards.

2. **Review**:
   - A maintainer will review your pull request and provide feedback.
   - Be prepared to make changes based on the feedback.

3. **Merge**:
   - Once approved, your pull request will be merged into the main branch.

---

## Thank You!

We appreciate your contributions and look forward to working with you. If you have any questions, feel free to open an issue or reach out to the maintainers.

Happy coding! 🚀
