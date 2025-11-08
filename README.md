<p align="center">
  <img src="app/src/main/res/drawable/ic_ledgerly.png" alt="Ledgerly Logo" width="300">

  <h1 align="center">Ledgerly</h1>
  <p align="center">Know your money.</p>

</p>

Ledgerly is a learning project . A personal finance management app built with Kotlin, Jetpack Compose, 
Room, and Firebase. It helps users track expenses, income, budgets, and goals with analytics, reports, and cloud sync, showcasing modern Android development.

This project was developed as part of a class assignment.
You can view the original assignment instructions here:
[SCO 306 - Project 2 (PDF)](./SCO%20306%20-project%202.pdf)

## Features

- Add and categorize transactions (income and expense)
- Track budgets and financial goals
- Local persistence using Room
- Basic analytics and simple reports


## Tech stack

- Kotlin
- Jetpack Compose (UI)
- AndroidX libraries
- Room (local database)
- Firebase
- Gradle

> **Note:**
> For simplicity, Ledgerly performs search and filtering operations entirely **in memory** rather than querying the database directly.
> This approach works well for small datasets during development and testing but may be replaced with database-backed queries in future versions.
