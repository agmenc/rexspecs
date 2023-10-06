# RexSpec

Executable specifications for any system with an API (HTTP/GraphQL/Events/Whatever)

### Stories

#### Run a Single Test

```
As a Developer,

I want to be able to run a single test from my IDE,

So that I can iterate towards completing my current task without having to run the whole damn test suite

Acceptance Criteria:
* Shift-F10 in IntelliJ runs the file against the app

```


```mermaid
sequenceDiagram
    autonumber

    actor Coder
    participant IDE
    participant Build Tool
    participant Test Framework
    participant RexSpec
    participant System Under Test
    participant Test Output

    rect rgb(50, 100, 50)
        Note over Coder, IDE: Trigger Single Test
        Coder ->> IDE: Shift-F10
        IDE ->> Test Framework: Run acceptance test
        Test Framework ->> RexSpec: Execute (hook)
        RexSpec ->> System Under Test: Start Web Server (hook)
        RexSpec ->> RexSpec: Find test in test resources folder
        RexSpec ->> RexSpec: Execute test (see next story)
    end
```

#### Execute Test

```
As RexSpec,

I want to be able to call API methods of the System Under Test,

So that I can compare the results with the expectations, and generate an output record

Acceptance Criteria:
* The Output Record is the same as the source file, but table cells are coloured, to
represent passing or failing tests 

```

```mermaid
sequenceDiagram
    autonumber

    participant RexSpec
    participant System Under Test
    participant Test Output

    rect rgb(50, 100, 100)
        Note over RexSpec, Test Output: Test Execution
        RexSpec ->> System Under Test: HTTP Post / GraphQL / Queue Drop
        System Under Test -->> RexSpec: Result
        RexSpec ->> RexSpec: compare expected result with actual
        RexSpec ->> Test Output: Decorate source file with colours for pass/fail/done
    end
```

#### Run All Tests

```
As a Developer,

I want to be able to run all my acceptance tests as part of the build,

So that I can commit knowing that the whole application works

Acceptance Criteria:
* The test task in the build fails if any acceptance test fails

```

```mermaid
sequenceDiagram
    autonumber

    actor Coder
    participant IDE
    participant Build Tool
    participant Test Framework
    participant RexSpec
    participant System Under Test
    participant Test Output
    
    rect rgb(50, 100, 50)
        Note over Coder, Build Tool: Trigger All Tests
        Coder ->> Build Tool: Run acceptance tests
        Build Tool ->> Test Framework: Run acceptance tests
        Test Framework ->> RexSpec: Execute (hook)
        RexSpec ->> System Under Test: Start Web Server (hook)
        RexSpec ->> RexSpec: Execute all tests in target folder
    end
```


