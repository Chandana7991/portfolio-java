# Chandana Lingala — Portfolio (Java Backend)

A personal portfolio site with a **pure Java backend** — no Spring, no Maven, no external
dependencies. It uses the JDK's built-in `com.sun.net.httpserver` to serve the site and a small
REST API. This means it compiles and runs with just a JDK install, anywhere.

## What's inside

```
portfolio-java/
├── src/
│   └── PortfolioServer.java   # backend: static file server + REST API
├── public/                    # frontend: HTML/CSS/JS (dark luxury theme)
│   ├── index.html
│   ├── css/style.css
│   ├── js/main.js
│   └── resume/Chandana_Resume.docx
└── data/
    └── contacts.jsonl         # contact form submissions land here (created automatically)
```

## API endpoints

| Method | Path            | Description                                  |
|--------|-----------------|-----------------------------------------------|
| GET    | `/api/projects` | Returns your projects as JSON                 |
| GET    | `/api/skills`   | Returns your skill categories as JSON         |
| POST   | `/api/contact`  | Accepts `{name, email, message}`, saves it    |

## Run it locally

You need a JDK (21+ recommended) installed — no Maven/Gradle needed.

```bash
cd portfolio-java
javac -d out src/PortfolioServer.java
java -cp out PortfolioServer
```

Then open **http://localhost:8080** in your browser.

Contact form submissions get appended as JSON lines to `data/contacts.jsonl` — open it any
time to see messages people have sent you.

## Editing content

- **Projects & skills**: edit the arrays directly in `src/PortfolioServer.java`
  (`ProjectsHandler` and `SkillsHandler` classes), then recompile.
- **Text, layout, styling**: edit `public/index.html` and `public/css/style.css` directly —
  no recompilation needed, just refresh the browser.
- **Resume file**: replace `public/resume/Chandana_Resume.docx` with an updated version
  (keep the same filename, or update the `href` in `index.html`).

## Deploying it for real (so it's live on the internet)

This currently only runs on your own machine (`localhost`). To make it a real, shareable
portfolio link, you have a couple of straightforward options:

1. **Any host that runs Java** — Render, Railway, or a small VPS. Point it at this repo,
   set the start command to the `javac`/`java` steps above, and it'll expose the same
   `http://localhost:8080` on a public URL instead.
2. **GitHub Pages for the frontend only** — if you'd rather not run a backend 24/7, the
   `public/` folder alone (HTML/CSS/JS) can be hosted for free on GitHub Pages, but you'd
   lose the live `/api/projects`, `/api/skills`, and contact-form endpoints unless you also
   deploy the Java backend somewhere.

Happy to help set up either path when you're ready.
