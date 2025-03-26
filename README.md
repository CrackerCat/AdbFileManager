# ADB File Manager

*Read this in [English](README.md) | [中文](README_zh.md)*

A Material You designed Android device file manager that enables remote file operations via ADB connection.

![Kotlin](https://img.shields.io/badge/kotlin-1.9.21-blue.svg)
![Compose](https://img.shields.io/badge/compose-1.5.11-green.svg)

## 📖 Introduction

ADB File Manager is a desktop application that allows you to remotely browse and manage Android device file systems via ADB connection. Built with a modern Material You interface using Jetpack Compose, it offers a smooth user experience and elegant animation effects.

## 📸 Preview

<div align="center">
  <img src="docs/images/1.png" alt="ADB File Manager Preview" width="800">
</div>

## ✨ Features

- 🎨 **Material You Design** - Modern UI design following Google's latest design language
- 📱 **Multi-device Support** - Connect to and manage multiple Android devices
- 📂 **Intuitive File Navigation** - File browser with path navigation bar, supporting path jump by clicking
- 📝 **File Editing** - Built-in text editor for editing text files on the device
- 🔒 **Permission Check** - Smart detection and prompting for file operation permission issues
- 🔄 **Real-time Feedback** - Real-time status feedback for all operations
- 📥 **File Transfer** - Support for downloading files from and uploading files to devices

## 🔧 Technology Stack

- **Kotlin** - Primary development language
- **Jetpack Compose** - Modern UI toolkit
- **ADB** - Android Debug Bridge tool
- **Coroutines** - Handling asynchronous operations
- **Flow** - Reactive data streams

## 🚀 Installation

### Building from Source

1. Clone the repository
   ```bash
   git clone https://github.com/AdbFileManager.git
   cd AdbFileManager
   ```

2. Build with Gradle
   ```bash
   ./gradlew build
   ```

3. Run the application
   ```bash
   ./gradlew run
   ```

## 💡 Usage Instructions

1. **Connect a device**:
   - Ensure USB debugging is enabled on your device
   - Connect the device to your computer
   - Select your device from the dropdown menu

2. **Browse files**:
   - Click on folders to enter them
   - Use the path navigation bar to quickly jump to parent directories
   - Click the refresh button to update the file list

3. **File operations**:
   - Click the menu button next to a file to see available actions
   - Edit: Modify text file content
   - Download: Save the file to your local computer
   - Delete: Remove the file from the device

4. **Create folder**:
   - Click the "New Folder" button in the toolbar
   - Enter a folder name and confirm

## 📋 Upcoming Features

- [ ] File search functionality
- [ ] File permission modification
- [ ] Dark/light theme toggle
- [ ] Drag and drop file upload
- [ ] File preview functionality

## 🤝 Contributing

Contributions, issue reports, and feature suggestions are welcome! See [CONTRIBUTING.md](CONTRIBUTING.md) for details.

1. Fork this repository
2. Create your feature branch (`git checkout -b feature/amazing-feature`)
3. Commit your changes (`git commit -m 'Add some amazing feature'`)
4. Push to the branch (`git push origin feature/amazing-feature`)
5. Submit a Pull Request

## 📄 License

This project is released under the MIT License - see the [LICENSE](LICENSE) file for details.
