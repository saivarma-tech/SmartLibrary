package smartlibrary.service;

import java.util.ArrayList;
import java.util.List;

public class SearchHistory {

    private List<String> history;


    // ==========================================
    // CONSTRUCTOR
    // ==========================================

    public SearchHistory() {

        history = new ArrayList<>();
    }


    // ==========================================
    // ADD SEARCH
    // ==========================================

    public void addSearch(String query) {

        if (query == null ||
            query.trim().isEmpty()) {

            return;
        }

        query = query.trim();


        // --------------------------------------
        // Avoid duplicate consecutive searches
        // --------------------------------------

        if (!history.isEmpty()) {

            String lastSearch =
                    history.get(history.size() - 1);

            if (lastSearch.equalsIgnoreCase(query)) {
                return;
            }
        }


        history.add(query);
    }


    // ==========================================
    // GET HISTORY
    // ==========================================

    public List<String> getHistory() {

        return new ArrayList<>(history);
    }


    // ==========================================
    // CLEAR HISTORY
    // ==========================================

    public void clearHistory() {

        history.clear();
    }


    // ==========================================
    // DISPLAY HISTORY
    // ==========================================

    public void displayHistory() {

        System.out.println();

        System.out.println(
                "================================="
        );

        System.out.println(
                "        SEARCH HISTORY"
        );

        System.out.println(
                "================================="
        );


        if (history.isEmpty()) {

            System.out.println();

            System.out.println(
                    "No search history."
            );

        } else {

            System.out.println();

            int number = 1;

            for (String query : history) {

                System.out.println(
                        number + ". " + query
                );

                number++;
            }
        }


        System.out.println();

        System.out.println(
                "================================="
        );
    }
}