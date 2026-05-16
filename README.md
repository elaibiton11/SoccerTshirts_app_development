# SoccerTshirts App

A robust Android application for soccer enthusiasts to browse, manage, and interact with a collection of soccer jerseys. Built with modern Android development practices, the app features real-time synchronization with Firebase and local caching with Room.

## 📱 Features

- **User Authentication:** Secure login and registration using Firebase Auth.
- **Jersey Management:** Browse a list of jerseys, view details, and add or edit your own jersey entries.
- **Interactive Comments:** Users can leave comments on specific jerseys to share their thoughts.
- **Profile Management:** Manage user profiles and see personalized content.
- **Image Integration:** Seamless image uploading via Cloudinary and high-performance loading with Picasso/Coil.
- **Offline Support:** Local data persistence using Room for a smoother user experience.

## 🏗 Architecture: MVVM

The project follows the **Model-View-ViewModel (MVVM)** architectural pattern, ensuring a clean separation of concerns:

- **View (Fragments/Activities):** Handles the UI and user interactions. Uses **ViewBinding** for safe view access and observes **LiveData** from ViewModels.
- **ViewModel:** Encapsulates UI logic and maintains state across configuration changes. Communicates with the Repository layer.
- **Repository:** Acts as a single source of truth for the ViewModels, orchestrating data flow between local Room database and remote Firebase Firestore.
- **Model:** Defines the data structures (`Jersey`, `Comment`, `UserProfile`).

## 📁 Project Structure

```text
com.example.soccertshirts_app/
├── data/
│   ├── local/          # Room DB: AppDatabase, DAOs, and Entities
│   ├── model/          # Data models: Jersey, Comment, UserProfile
│   ├── repository/     # Repositories: AuthRepository, JerseyRepository
│   └── services/       # Cloudinary integration for image management
├── viewmodel/          # ViewModels for Auth, Home, Profile, Comments, and Add/Edit
├── adapters/           # JerseyAdapter, CommentAdapter (RecyclerView logic)
└── ui/                 # Fragments (Home, Login, Register, Profile, etc.)
```

## 🛠 Tech Stack & Libraries

- **Kotlin:** Primary programming language.
- **Jetpack Navigation:** Handles app navigation with Type-safe arguments (Safe Args).
- **Firebase:**
    - **Auth:** For user session management.
    - **Firestore:** NoSQL cloud database for real-time data.
- **Room:** SQLite abstraction layer for local caching.
- **Cloudinary:** Cloud-based image management for uploading jersey photos.
- **Picasso & Coil:** Efficient image loading and caching.
- **Coroutines:** Asynchronous programming for non-blocking UI.

## 🚀 Getting Started

1. **Prerequisites:**
   - Android Studio Iguana or newer.
   - A Firebase project with `google-services.json` placed in the `app/` folder.
   - Cloudinary account credentials.

2. **Configuration:**
   - Update `CloudinaryModel` with your API credentials if necessary.
   - Ensure `minSdk` 24 is met by your testing device/emulator.

3. **Build & Run:**
   - Sync the project with Gradle files.
   - Run the `:app` module.

## 🤝 Design Patterns Used

- **Singleton:** Used for the Room `AppDatabase` instance.
- **Observer:** Implemented via `LiveData` to update the UI automatically when data changes.
- **Repository Pattern:** To abstract data sources from the rest of the app.
- **Adapter Pattern:** For managing list displays in `RecyclerView`.
