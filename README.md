# checkers
Checkers app that uses drag and drop to play. You can play online or agianst a bot. 

## Features
- Uses Firebase realtime database for online games.
- bot that can be played against when offline.
- google one touch sign in and anonymouse sign in.
- drag and drop to move the checkers pieces.

## Tech Stack 
- [Koin](https://insert-koin.io/)
- [Lottie Compose](https://github.com/airbnb/lottie/blob/master/android-compose.md)
- [Coroutines + Flow](https://kotlinlang.org/docs/coroutines-overview.html)
- [Jetpack Compose](https://developer.android.com/jetpack/compose)
- [Appyx](https://bumble-tech.github.io/appyx/)
- [Firebase Realtime Database](https://firebase.google.com/products/realtime-database)
- [Firebase Auth](https://firebase.google.com/products/auth)


## Project Architecture
The App uses MVVM architecture and exposes the data using a unidirectional flow down to the jetpack comopse UI. this is accomplished
through kotlin flows. The ui calls viewmodel function which interact with the Firebase realtime database through Usecases injected using Koin. 
The app also uses gradle kts with a TOML file to specify dependencies.


## App Images


<img src="https://github.com/SilvVF/checkers/assets/98186105/f5f3df07-d88c-4d48-9961-6efa34026593" width=300>

<img src="https://github.com/SilvVF/checkers/assets/98186105/ce689456-42c8-40d6-9c6b-232ffb8b029b" width=300>

<img src="https://github.com/SilvVF/checkers/assets/98186105/c8744cee-56dd-40bc-a030-215eab8c7fcf" width=300>

<img src="https://github.com/SilvVF/checkers/assets/98186105/5ebe1a2e-2c5a-435b-bde7-162b295aea78" width=300>

<img src="https://github.com/SilvVF/checkers/assets/98186105/a14cee63-d38e-4f96-9b32-8b27f4e69ba4" width=300>
