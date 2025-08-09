# Contributing to Stoxie

We love your input! We want to make contributing to Stoxie as easy and transparent as possible, whether it's:

- Reporting a bug
- Discussing the current state of the code
- Submitting a fix
- Proposing new features
- Becoming a maintainer

## Development Process

We use GitHub to host code, to track issues and feature requests, as well as accept pull requests.

1. **Fork the repo** and create your branch from `main`
2. **If you've added code** that should be tested, add tests
3. **If you've changed APIs**, update the documentation
4. **Ensure the test suite passes**
5. **Make sure your code lints**
6. **Issue that pull request!**

## Code Style Guidelines

### Kotlin Conventions

- Follow [Kotlin coding conventions](https://kotlinlang.org/docs/coding-conventions.html)
- Use meaningful variable and function names
- Add KDoc comments for public APIs
- Follow the existing architecture patterns (MVVM, Repository)

### Android Best Practices

- Use View Binding instead of findViewById
- Follow Material Design guidelines
- Implement proper error handling
- Use coroutines for asynchronous operations

### Code Organization

- Group related functionality in packages
- Use dependency injection where appropriate
- Keep ViewModels lean and focused
- Separate UI logic from business logic

## Pull Request Process

1. **Update the README.md** with details of changes if needed
2. **Update the version numbers** in any examples files and the README.md to the new version that this Pull Request would represent
3. **You may merge the Pull Request** in once you have the sign-off of two other developers, or if you do not have permission to do that, you may request the second reviewer to merge it for you

## Issues and Bug Reports

**Great Bug Reports** tend to have:

- A quick summary and/or background
- Steps to reproduce
  - Be specific!
  - Give sample code if you can
- What you expected would happen
- What actually happens
- Notes (possibly including why you think this might be happening, or stuff you tried that didn't work)

## Feature Requests

We track feature requests using GitHub issues. When filing a feature request:

- Clearly describe the feature
- Explain why this feature would be useful
- Provide examples of how it would be used
- Consider whether this feature fits with the project's goals

## Code of Conduct

By participating, you are expected to uphold this code. Please report unacceptable behavior to [your.email@example.com].

### Our Pledge

In the interest of fostering an open and welcoming environment, we as contributors and maintainers pledge to making participation in our project and our community a harassment-free experience for everyone.

### Our Standards

Examples of behavior that contributes to creating a positive environment include:

- Using welcoming and inclusive language
- Being respectful of differing viewpoints and experiences
- Gracefully accepting constructive criticism
- Focusing on what is best for the community
- Showing empathy towards other community members

## License

By contributing, you agree that your contributions will be licensed under its MIT License.

## Getting Started

1. **Set up your development environment** following the setup instructions in README.md
2. **Check the issue tracker** for good first issues
3. **Join the discussion** on existing issues or start a new one
4. **Submit your first pull request!**

## Testing

- Write unit tests for new functionality
- Ensure existing tests pass
- Test on multiple device configurations when possible
- Include integration tests for API interactions

## Documentation

- Update code comments for any changed functionality
- Update README.md if you change installation or usage instructions
- Add inline code documentation for complex algorithms
- Update API documentation for any new endpoints or parameters

Thank you for contributing to Stoxie! 🚀
