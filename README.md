# 🩸 Blood Bank Finder — DevOps Lab Project

**Problem:** In emergencies, families call blood bank after blood bank to find a matching group.
**Solution:** A microservice where blood banks post their stock and patients search by city + blood group.
It also returns **compatible** groups (e.g. an A+ patient can take A+, A-, O+, O-) and flags **low stock** (< 5 units).

**Stack:** Spring Boot (backend) · HTML/JS (frontend) · GitHub · Jenkins · Docker · Kubernetes · Karate + Spring Boot tests

---

## Project structure (what each file does)

| File | What it does |
|---|---|
| `BloodBankApplication.java` | Starts the app (port 8080) |
| `Stock.java` | Data model: bank, city, group, units (+ `low` flag) |
| `BloodService.java` | **Business logic**: compatibility chart, search, add/update with validation |
| `BloodController.java` | REST API: `GET /api/search`, `POST /api/stock`, `GET /health` |
| `static/index.html` | Frontend: search form + update stock form (calls the API with `fetch`) |
| `BloodBankTests.java` | Spring Boot tests (unit + MockMvc API tests) |
| `karate/blood-api.feature` | Karate API tests |
| `karate/donor-eligibility.js` + `js-logic.feature` | Simple JS program tested with Karate |
| `Dockerfile` | Multi-stage build: Maven builds the jar → small JRE image runs it |
| `k8s.yaml` | Kubernetes Deployment (2 pods + health check) and NodePort Service |
| `Jenkinsfile` | CI/CD: checkout → build → test → docker build → deploy to k8s |

## How the request flows
```
Browser (index.html) --fetch--> BloodController --> BloodService --> in-memory list
                     <--JSON---
```

---

## How to run (step by step)

**1. Run locally**
```bash
mvn spring-boot:run
# open http://localhost:8080
```

**2. Run tests (Spring Boot + Karate)**
```bash
mvn test
# Karate HTML report: target/karate-reports/karate-summary.html
```

**3. Docker**
```bash
docker build -t blood-bank:latest .
docker run -d -p 8080:8080 --name bb blood-bank:latest
docker ps                     # check it is running
docker logs bb                # see app logs
docker stop bb && docker rm bb
```

**4. Kubernetes** (Docker Desktop Kubernetes or Minikube)
```bash
# Minikube only: make the local image visible inside minikube
minikube image load blood-bank:latest

kubectl apply -f k8s.yaml
kubectl get pods              # should show 2 pods Running
kubectl get svc
# Docker Desktop: open http://localhost:30080
# Minikube:       minikube service blood-bank-service
```
Self-healing demo: `kubectl delete pod <pod-name>` → Kubernetes creates a new one automatically.
Scaling demo: `kubectl scale deployment blood-bank --replicas=4`

**5. Git + GitHub**
```bash
git init && git add . && git commit -m "Initial blood bank service"
git branch -M main
git remote add origin https://github.com/<you>/blood-bank.git
git push -u origin main
# feature work: git checkout -b feature/low-stock  → push → open Pull Request → review → merge
```

**6. Jenkins**
New Item → **Pipeline** → "Pipeline script from SCM" → Git → your repo URL → Script path `Jenkinsfile`.
Tick **GitHub hook trigger** (or Poll SCM `H/5 * * * *`) so every push auto-runs the pipeline.
*Windows Jenkins: change `sh` to `bat` in the Jenkinsfile.*

---

## User stories (4-member split)

| # | Member role | User story | Files owned |
|---|---|---|---|
| 1 | Backend Developer | As a patient's family, I want to search compatible blood by city so I find it fast. | `BloodService`, `BloodController`, `index.html` |
| 2 | Docker & K8s Engineer | As a DevOps engineer, I want the app containerized and run on Kubernetes so it self-heals and scales. | `Dockerfile`, `k8s.yaml` |
| 3 | QA / Test Engineer | As a QA engineer, I want automated Karate + Spring Boot tests so bugs are caught before deploy. | `BloodBankTests`, `karate/*` |
| 4 | CI/CD Lead | As a team, we want every GitHub push to build, test and deploy automatically. | `Jenkinsfile`, GitHub repo/PRs |

---

## Likely viva questions (short answers)

- **Why microservice?** It's one small, independent service with its own API; it can be deployed and scaled alone.
- **Why `@Service` and `@RestController` separate?** Controller handles HTTP only; service holds the logic → easier to test.
- **Why in-memory list, not a DB?** Keeps the demo simple; can swap for MySQL with Spring Data JPA without changing the controller.
- **Why `encodeURIComponent` in JS?** `+` in a URL means a space, so `A+` would arrive as `A `.
- **Why multi-stage Dockerfile?** Build tools (Maven, JDK) stay in stage 1; final image only has the JRE → much smaller.
- **Deployment vs Service in k8s?** Deployment keeps N pods running; Service gives them one stable address + load balancing.
- **What is livenessProbe?** k8s calls `/health` every 10s; if it fails, the pod is restarted.
- **Karate vs Spring Boot test?** Spring Boot tests check Java code from inside (MockMvc, no real network); Karate tests the running API from outside using readable Given/When/Then.
- **What does `RANDOM_PORT` do?** Starts the real server on a free port so Karate can send real HTTP requests to it.
- **What is `Map.of(...)` in BloodService?** A lookup table: key = patient's group, value = list of groups they can receive. Search just checks `donors.contains(bank's group)`.
- **Why an empty constructor in `Stock`?** When JSON comes in (POST), Spring creates an empty `Stock` and fills it using the setters.
- **Where does `"low"` in the JSON come from?** Spring turns every getter into a JSON field; `isLow()` becomes `"low"`.

---

## The 2 trickiest parts, explained

### KarateRunnerTest + karate-config.js
1. Karate sends **real** HTTP requests, so the app must be running.
2. `@SpringBootTest(webEnvironment = RANDOM_PORT)` starts the whole app on a free port before the tests.
3. `@LocalServerPort int port;` → Spring tells us which port it chose.
4. `System.setProperty("app.port", ...)` passes it on; `karate-config.js` reads it and sets `baseUrl = http://localhost:<port>`.
5. `Karate.run("blood-api", "js-logic")` runs both feature files.
Result: `mvn test` (and Jenkins) runs all tests with **no manual setup**.

### @ExceptionHandler in BloodController
- The service throws `IllegalArgumentException` for bad input (e.g. units = -5).
- Without a handler, Spring would return a generic 500 error.
- With it, Spring calls `badRequest(...)`, which returns **HTTP 400** + `{"error": "Units cannot be negative"}`.
- The webpage shows that message; the Karate test checks it (`status 400`, `match response.error == ...`).
