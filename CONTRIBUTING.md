# Contributing to Flight Search Service

Thank you for your interest in contributing to the Flight Search Service! We welcome contributions from the community.

## Getting Started

1. Fork the repository
2. Clone your fork locally
3. Set up the development environment (see README.md)
4. Create a new branch for your feature or bug fix

## Development Workflow

### Before Making Changes

1. **Format your code**: Always run Spotless before committing
   ```bash
   mvn spotless:apply
   ```

2. **Check formatting**: Ensure code follows our style guidelines
   ```bash
   mvn spotless:check
   ```

3. **Run tests**: Make sure all tests pass
   ```bash
   mvn test
   ```

### Making Changes

1. Create a descriptive branch name:
   ```bash
   git checkout -b feature/your-feature-name
   # or
   git checkout -b fix/issue-description
   ```

2. Make your changes following our coding standards
3. Add tests for new functionality
4. Update documentation as needed

### Commit Guidelines

- Use clear, descriptive commit messages
- Follow the conventional commit format:
  - `feat: add new feature`
  - `fix: resolve bug in search functionality`
  - `docs: update README with new instructions`
  - `style: apply code formatting`
  - `test: add unit tests for service layer`

### Code Style

This project uses **Spotless** with Google Java Format:
- All code must pass `mvn spotless:check`
- Use meaningful variable and method names
- Add JavaDoc comments for public methods
- Follow Spring Boot best practices

### Pull Request Process

1. Ensure your branch is up to date with main
2. Run the full test suite: `mvn clean test`
3. Check code formatting: `mvn spotless:check`
4. Push your branch to your fork
5. Create a Pull Request with:
   - Clear title and description
   - Reference any related issues
   - Include screenshots for UI changes
   - List any breaking changes

### Testing Requirements

- Write unit tests for new functionality
- Ensure integration tests pass
- Maintain or improve code coverage
- Test with all supported databases (MySQL, Elasticsearch, Redis, Neo4j)

## Code Review Process

All submissions require review before merging:
1. At least one maintainer approval
2. All CI checks must pass
3. Code formatting compliance
4. Test coverage requirements met

## Reporting Issues

When reporting issues, please include:
- Clear description of the problem
- Steps to reproduce
- Expected vs actual behavior
- Environment details (Java version, database versions)
- Relevant logs or error messages

## Questions?

Feel free to open an issue for questions about contributing or reach out to the maintainers.

Thank you for contributing! 🚀
