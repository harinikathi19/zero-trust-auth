Zero Trust Authentication with Risk Analysis

📌 Description
Developed a Zero Trust authentication system using JWT, OTP, device fingerprinting, and risk-based access control to enable secure and adaptive user authentication.

📖 Overview
This project implements a Zero Trust Authentication system that continuously verifies users instead of assuming trust after login.It combines JWT authentication, OTP-based verification, device fingerprinting, and risk-based access control to enhance application security and detect suspicious behavior.

🚀 Features
🔐 JWT-based Authentication
📲 OTP-based Multi-Factor Authentication
🖥️ Device Fingerprinting
⚠️ Risk-Based Access Control
🚦 Rate Limiting & Security Filters
🔍 Anomaly Detection

🛠️ Tech Stack

 BACKEND
Java
Spring Boot
Spring Security
JWT
Maven

 FRONTEND
HTML
CSS
JavaScript


⚙️ Setup & Run
1️⃣ Clone Repository
git clone https://github.com/your-username/zero-trust-auth.git
cd zero-trust-auth

2️⃣ Run Backen
cd backend
./mvnw spring-boot:run

Windows:
.\mvnw.cmd spring-boot:run

Backend runs at:
http://localhost:8080

3️⃣ Run Frontend
cd ../frontend

Open index.html directly
OR
Use VS Code Live Server

Frontend runs at:
http://127.0.0.1:5500

4️⃣ API Configuration
Ensure frontend API calls point to:
http://localhost:8080/api

🧠 How It Works

User logs in with credentials
JWT authentication is performed
OTP verification is triggered
Device fingerprint is validated
Risk analysis checks for anomalies
Access is granted or denied based on risk

📂 Project Structure
backend/
  ├── controllers/]
  ├── services/
  ├── security/
  ├── models/
  └── config/

frontend/
  ├── pages/
  ├── js/
  └── assets/

🎯 Use Cases
Secure login systems
Banking & financial applications
Admin dashboards
High-security web applications

🔮 Future Improvements
AI-based risk analysis
Geo-location anomaly detection
Biometric authentication
Real-time alerts

📝 Author
Zero Trust Authentication Project
