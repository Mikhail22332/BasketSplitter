
# Basket Splitter

Basket Splitting is an algorithm designed to optimize the delivery process for an online supermarket. It aims to divide the items in a customer's shopping cart into delivery groups to minimize the number of required deliveries. Moreover, the algorithm tries to maximize the number of items in each group.




## The Algorithm Description 
The solution for that problem is inspired by the Heuristic Algorithm Local Search, Tabu Search and Set Cover Problem. We can divide the algorithm into 3 main parts.

- The first phase is responsible for creating a primary delivery group for a given customer's cart. A primary delivery pool is created based on the max heap, which stores information about how many items can be delivered by each delivery company. 
- The second phase is a minimization step. Starting from the smallest delivery group the algorithm tries to rearrange elements to other groups. If all elements from the taken group can be rearranged into other groups, also deletes the group. Otherwise, the group would be added to the tabu list.
- The last phase aims to maximize the number of items in delivery groups after each minimization step. Starting from the largest delivery group, the algorithm identifies common items shared with other groups and removes them. This ensures that each item is uniquely assigned to a single delivery group, maximizing efficiency.
## Problem Variations
The solution I've mentioned above is a nondeterministic algorithm which can be attributed to the family of NP-hard problems. The correctness of the first phase (minimization phase) can be proven based on the Set Cover Problem. Notice, when the primary delivery pool is populated there is a guarantee that all required groups with unique elements would be present in the pool. 

Although, we don't have a guarantee, that maximization step would give us groups of the maximum possible number of items for each group. This nondeterministic part is connected with the max heap constructed in the 1 phase.

Example: A-28, B-24, C-23, D-23, E-23, F-18, G-16.

In that example, companies: C, D and E have the same number of items. For a deterministic solution, we would have needed to consider all 6 permutations of CDE part. So in total, we would have 6 max heaps. Based on that fact deterministic solution has O(n!) time complexity, where n is number of companies.  
## Technologies Used

- Java 17
- Json Simple toolkit
- JUnit Jupiter
- JavaFX
