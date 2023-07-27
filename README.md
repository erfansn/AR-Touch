# 🔮  AR Touch
Obtain the power of **touchless interaction!** Being able to effortlessly navigate and interact with various screen displays **without** any physical contact, is what this project brings you.

While [touchless technology](https://www.greetly.com/blog/what-is-touchless-technology) exists, it often remains out of reach for many due to its limited availability and high costs. 
Our mission with AR Touch is to bridge this gap and democratize touchless interaction for everyone.
By implementing this ***cutting-edge technology*** only in a user-friendly Android app, we enable you to interact with any screen,
regardless of whether it supports touch input or its dimensions.

# Features
- It is *independent of the target operating system*, that is, there is no need to install a special interface software (I already installed it for you!)
and it has been tested on **Windows** and **Android** operating systems.
- All the processes are done in the device itself, so there is no need to spend extra money to buy an additional tool.

# The magic behind it
When we place our fingers on the touchscreen, occurred an interrupt to handle touch, which the operating system must handle. 

Since we need to have the touch event first for the OS to handle it,
we must first determine the touchable area. For this purpose, I have utilized [ArUco Markers](https://docs.opencv.org/4.x/d5/dae/tutorial_aruco_detection.html) to identify the edges of the screen frame. 

Additionally, I have used hand gestures to generate events, and for their respective computation, I have employed on-device ML-powered solutions available within the [MediaPipe framework](https://developers.google.com/mediapipe).

To deliver the touch event independently of the OS, I have utilized [BLE technology](https://novelbits.io/bluetooth-low-energy-ble-complete-guide/) and the [HID protocol](https://en.wikipedia.org/wiki/Human_interface_device).
![The magic behind it](/media/the_magic_behind_it.png)

## 🔍 Prerequisites
1. A device which Android 9 was *init version*
   - with mid-range processor e.g. `Snapdragon 730`
2. Four ArUco markers, can be downloaded [here](producer/markers)

## 🚫 Limitation
- Only one hand can be placed in the frame of the screen, so currently zooming is not available as usual.
- Currently, the touch event is generated in line with the content displayed on the phone screen.

## 🎬 Demo
[![Youtube Demo](/media/youtube.png)](https://www.youtube.com/watch?v=woEX1JKgeAo)

## 🛠️ Technology Stack
- Kotlin
- C/CPP
- View System
- Jetpack Compose
- CameraX
- NDK and JNI
- OpenCV
- MediaPipe
- Coroutines
- Flow
- Bluetooth Low Energy
- Koin

## 🏗️ Architecture
Uses modularization approach and follows the [official architecture guidance](https://developer.android.com/topic/architecture).
![Architecture diagram](/media/architecture_diagram.png)

## 🧪 Testing & Quality Assurance
Testing has been performed in this project **without** using common frameworks for mocking, relying solely on *dummy, stub, shadow, and fake* test doubles.

*Robolectric*, a powerful simulator, has been utilized to take advantage of its benefits in executing **UI** and **Integration** tests.

To execute Isolated **Instrumentation** tests, the *Orchestrator* tool has been enabled in Gradle.

To enhance the components of the producer module, **Performance** tests have been considered using the *Microbenchmark* tool. Additionally, to prevent code leaks in production, a separate module has been designated for them.

You can see the list of tested classes [here](https://github.com/ErfanSn/AR-Touch/issues/16).

## 📚 More about
If you want to know about the process of evolution and other aspects of this project, you can read the project report. 
This project was for my undergraduate degree at the *University of Tabriz*.
- [English version](https://docs.google.com/document/d/1ENNQMLADxYiPcoJ0-0cCe4P9LCgyyeuu/edit?usp=sharing&ouid=115301471611369797131&rtpof=true&sd=true)
- [Persian version](https://docs.google.com/document/d/1lADQhTCvIjpkDAU-ZE4bby3WU_DpsMjH/edit?usp=sharing&ouid=115301471611369797131&rtpof=true&sd=true)

## 🤝 Contributation
I welcome and appreciate contributions from the community to improve the AR Touch project. 
If you have any ideas, suggestions, or feedback to enhance the project, feel free to share them in the Discussions section. 

You can actively participate by:
- Opening `discussions` to propose new features or improvements.
- Creating `new issue` to report the bugs you encounter during testing.
- Submitting `pull requests` to introduce code enhancements or optimizations.

## 💖 Sponsors and Supporters 
Thank you for taking the time to read about the AR Touch project! If you find the project fascinating, your financial support would be truly heartwarming.

By clicking on the **Sponsor** button right now, you can become a valuable partner in the project's evolution. 
Every donation, no matter the amount, makes a significant impact and inspires us to push the boundaries of what's possible. 

Join us in shaping the future of touchless interactions with your generous support. Let's make magic together! 🌟

## 📜 License

**AR Touch** is distributed under the terms of the Apache License (Version 2.0).
See the [license](LICENSE) for more information.
