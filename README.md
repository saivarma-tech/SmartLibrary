# SmartLibrary Complete

SmartLibrary is a local Java 21 library app with a responsive browser interface. Members can search and filter books and research papers, create an account, borrow available books, renew or return loans, and join or cancel a waiting list. The librarian desk supports book create/edit/archive, member records, loan and overdue views, reservations, subject-overlap review, and aggregate search activity.

The webpage calls the Java HTTP API. Catalogue items, password hashes, loans, reservations, members, and search events persist in `data/smartlibrary.tsv`, created on first run. The store uses only the Java standard library; the starter catalogue is loaded once if no store file exists. Back up that file before clearing or moving project data.

## Import and run in Eclipse

1. Extract `SmartLibrary_Complete.zip` to a folder.
2. In Eclipse choose **File → Import → General → Existing Projects into Workspace**.
3. Choose the extracted `SmartLibrary_Complete` folder and finish the import. The project requires a Java 21 JDK.
4. Run `SmartLibrary Web.launch` (or right-click `src/smartlibrary/web/LibraryServer.java` and choose **Run As → Java Application**).
5. Open [http://localhost:8080](http://localhost:8080) in a browser. The server binds only to the local computer. If port 8080 is busy, stop the other local server or edit the launch arguments to use another port.
6. Create a member account to borrow books and manage personal loans.

The **Librarian desk** asks for a staff key. For local classroom use the default is `library-admin`. Before allowing other people to access the computer, set the `SMARTLIBRARY_ADMIN_KEY` environment variable in the Eclipse launch configuration to a private value. Do not expose this development server to the internet; it uses local HTTP and is not a production hosting setup.

## Checks

The source includes dependency-free Java `*Test` programs. In Eclipse, run each test class as a Java application. They cover the existing algorithm demonstrations plus JSON parsing and persistent-data reload. The API was also exercised locally for registration/login, catalogue filtering and spelling suggestions, copy availability, borrowing, renewal limits, returns, waiting-list order and promotion, separate member accounts with identical display names, staff authorization, subject review, related titles, usage reports, and persistence across server restart.

## DSA notes

See [DSA_FEATURE_MAP.md](DSA_FEATURE_MAP.md) for the member tasks powered by DSA and an honest list of coursework utilities that remain standalone or use illustrative data.

## Local demo limitations

- This is a single-computer teaching/demo app with a TSV store, not a production database or multi-branch deployment.
- Members register with email and a password hash, but there is no email verification, password recovery, account deactivation, or fine-grained librarian account system. Librarian access uses one shared local key.
- The catalogue currently supports book administration and seeded research papers. Paper editing, journals, ISBN checksum validation, circulation scanning, email notifications, payment/fines, and audit exports are not implemented.
- Reviewer matching and branch flow in the Java console still use illustrative networks because no reviewer/branch inventory entities exist yet. The web librarian review suggestion is based on identical subject labels.
- Search terms are retained in the local store for librarian activity reports; do not enter sensitive personal information into searches.
