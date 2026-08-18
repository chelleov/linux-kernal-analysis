# Scala Style Guidelines

The scala code should follow these adapted java guidelines.

You can find a short video clip (in hebrew) that goes over the style guidelines listed in this document at https://www.youtube.com/watch?v=ruZy8gUjX_0.

## Comments

1. You should place scaladoc comments for every class, every object, every enum, every exception, every method, and every variable (excluding local variables). We can avoid scaladoc comments when dealing with private classes, methods, and variables.

```scala
/**
 *
 *
 *
 */
```

You can take example for proper usage of scaladoc comments browsing the source code in src.zip. You can find a tutorial that explains how to write scaladoc comments at http://www.oracle.com/technetwork/articles/java/index-137868.html.

2. You can (where appropriate) place c styled comment at the beginning of the function in order to provide detailed information about the algorithm or the way you chose to implement the function.

```scala
def doSomething(): Unit =
{
        /*
        *
        *
        */
        ...
}
```

3. You should place c++ styled comments before every bunch of code (group of lines) in order to keep the code organized and clear.

```scala
//creating gui components
bt = new JButton("ok")
tf = new JTextField(10)

//adding events listeners
bt.addActionListener(...)
```

## Identifiers

1. Variables and Methods names should be composed of small letters only. If the variable name includes more than one word then every word (starting with the second word) will start with a capital letter

```scala
val numOfStudents = 12
```

If the variable/method name includes abbreviation, each letter should be capitalized (except for those cases in which the entire name is an abbreviation).

2. Class/Object/Exception/Trait names should start with a capital letter. If the name includes more than one word then every word will start with a capital letter

```scala
class SportCar {}
```

If the class/object/exception/trait name includes abbreviation, each letter should be capitalized.

3. Names of packages should start with the domain name (opposite direction) of the company that develops the package. In addition, the package name should include small letters only.

e.g. `com.lifemichael.samples`, `il.ac.hit.samples`...

## Classes

1. Make sure your class includes the definition for a primary constructor. Make sure all other constructors use the primary one.

2. Make sure to include validation tests inside the setters. Make sure the constructor uses the setters. Avoid direct assignments to the variables. The validation tests should be inside the setters only. Avoid duplicate code in the constructors. Make sure you define a property (not a setter or a getter).

3. Make sure the code of your class is organized properly: first we declare the variables.. then the constructors... and the methods come right after. Make sure you follow the common order we know from the java api.

4. Make sure that whenever you override the equals method you also override hashCode and make sure each one of the two methods works according to the other one.

5. It is a good practice to override the toString method.

6. When implementing Cloneable make sure you override the clone method.

7. The access modifier for each and every variable you declare should be private unless there is a good reason for something else.

8. Make sure each method starts with validating the arguments it received.

## Traits

1. Make sure you declare a trait and a separated class that implements it.

2. Wherever you need a variable that should hold a reference for a specific object the variable type should be a trait (not a class) where possible.

```scala
val currencies: List[Currency] = new LinkedList[Currency]()
```

3. Prefer using trait over abstract class.

## Strings

1. When relevant prefer using StringBuilder.

2. Prefer using strings by writing them explicitly... e.g. `"abc"` (it is better than doing `new String("abc")`)

## Memory Management

1. When there is no need for a specific object make sure you assign null to every variable that holds its reference.... so the garbage collector will be able to clean the memory been used by the object.

2. Don't count on finalize()

## Generics

1. Whenever you use a generic class make sure you write your code accordingly.

2. Prefer using bounded wildcard when possible.

## Separation of Concerns

1. Make sure to keep a clear separation between the project parts. Make sure each part doesn't interfere with other parts responsibility.

e.g. If you include in your model code that responsible to the ui it would be a violation of the clear separation we look for.

## User Interface

1. Each and every interaction with the user interface should be within the EDT thread.

## Functional Core

1. Implement all transformations (e.g., filtering, grouping, summarizing) as pure functions.

2. Use immutable data structures (e.g., case classes, List, Map) when possible.

3. Use currying or partial functions where applicable.

4. Demonstrate function composition when applicable.

## Apache Spark Usage

1. Use the RDD API or the DataFrame API or the DataSet API.

2. You should use at least four different Spark operations (e.g., map, filter, reduceByKey, groupBy, join).

3. You should load data from an external source and save the results to a file.

## Advanced Functional Programming

You should include and document at least three of the following:

1. A custom combinator.

2. Use of closures in Spark transformations.

3. A tail-recursive function for computation.

4. Pattern matching with case classes.

5. Functional error handling.

## Documentation & Testing

1. Inline comments and ScalaDoc should be added.

2. Unit testing should be added using ScalaTest.

3. You should separate the pure logic from the I/O operations. The separation should be clear.
