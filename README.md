#Basket Splitting
	Basket Splitting is a library designed to optimize the delivery process for an online supermarket. It aims to divide the items in a customer's shopping cart into delivery groups to minimize the number of required deliveries. By considering factors such as item size, delivery method availability, and customer preferences, the library ensures efficient and cost-effective delivery operations. It utilizes a configuration file to determine available delivery methods for each product and provides an API for seamless integration into existing systems. With Basket Splitting, online supermarkets can streamline their delivery processes and enhance customer satisfaction.

Dependencies
	Java 17
	JSON.simple
	JUnit Jupiter
	JavaFX (if applicable)

Metaheuristic Algorithm for Basket Splitting
	The Basket Splitting library utilizes a metaheuristic algorithm to efficiently divide items in a customer's shopping cart into delivery groups. The algorithm consists of three phases:

	1. First Phase:
In the first phase, the algorithm populates a dictionary based on a max heap for the given basket. It iterates over each item in the basket and assigns it to the company that can deliver the maximum number of items. The delivery pool dictionary contains company names as keys and lists of items as values, representing which items each company will deliver.

	2. Second Phase:
The second phase attempts to rearrange the delivery pool to have smaller groups, reducing the number of companies required to deliver all items from the basket. It starts from the smallest groups and goes to the largest. If no items are found for a specified company in the delivery pool, a NullPointerException is thrown.

	3. Third Phase:
In the third phase, the algorithm aims to optimize the delivery pool by redistributing products from dominant groups to other groups, maximizing the number of elements in each group by rearranging groups. The tabu list helps avoid repeating actions and considering already checked companies.

Metaheuristic Algorithm:
	A metaheuristic algorithm is a high-level problem-independent algorithmic framework that provides a set of guidelines or strategies to develop heuristic optimization algorithms. 
 These algorithms are used to solve complex optimization problems that cannot be efficiently solved with exact algorithms. Metaheuristic algorithms are iterative and work by iteratively improving candidate solutions based on a defined objective function. They are often employed in combinatorial optimization problems, such as basket splitting, due to their ability to find good solutions in a reasonable amount of time.

