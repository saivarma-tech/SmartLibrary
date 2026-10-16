# SmartLibrary DSA feature map

Members and librarians choose everyday library tasks. These algorithm names are for the project presentation and code review; they are not labels in the member interface.

| Member or staff task | DSA used internally | Where it is connected |
|---|---|---|
| Search book title, author, subject, ISBN, or research abstract | KMP, Z-function, Rabin–Karp, Aho–Corasick; priority-queue ranking | `SearchService` feeds `/api/catalogue`, which powers the web search and filters. |
| Offer a spelling suggestion | Levenshtein distance | `QueryCorrectionService` is used by the catalogue API when a search has no match. |
| Find similar catalogue items | Suffix array + Kasai LCP | `DocumentIndexService.findRelatedTo` backs each card's “Similar titles” action. Search phrase completion in the console uses the suffix automaton. |
| Recommend titles and show popular books | Randomized QuickSort | `/api/discover` ranks borrowing counts and member subject history. |
| Borrow, return, renew, and reserve | Persistent lists and fair FIFO queue | `LibraryData` stores loans and reservations; reservation promotion uses a priority queue ordered by request time. Renewals stop when the title has a waiting member, the loan is overdue, or two renewals were used. |
| Review records with shared subject labels | Graph model + vertex-cover 2-approximation | The librarian endpoint and Main's staff workflow suggest records from matching categories. This is a category overlap rule, not semantic document similarity. |
| Summarize catalogue searches | Randomized QuickSort, reservoir sampling, parallel reduce, Blelloch scan | Librarian activity insights rank search terms, sample the recorded stream, total events, and calculate cumulative counts. Search queries are stored for aggregate staff reporting. |
| Assign papers to reviewers and estimate resource flow | Bipartite matching, Edmonds–Karp, Dinic | Available in the Java console's staff workflow, but its reviewer eligibility and branch network are illustrative because the current data model has no reviewer or branch records. |

## Academic implementations that remain independent utilities

The algorithms package preserves SAT/3-SAT, 3-SAT-to-CLIQUE and graph reductions, Miller–Rabin, Brent's theorem, Damerau–Levenshtein, sequence alignment, matrix-chain multiplication, bitmask selection, and optimal-BST routines. They are not all forced into a member workflow where they do not fit. Existing `*Test` classes exercise several of these utilities.

The current `SuffixArray` implementation uses prefix doubling, with O(n log² n) construction in this implementation. SA-IS is not implemented. The usage and catalogue data are intended for a local educational instance, not a multi-server deployment.
