# RexSpecs - Executable Specifications Made Easy

[Full Docs](https://agmenc.github.io/rexspecs/)

Tables are a visually-dense, clear and systematic way of presenting information. This makes them excellent for writing tests. RexSpecs automates their execution.

### Calculator Example

Imagine you are writing a calculator service. You input two numbers and an operator, call the 
service over HTTP, and receive a result. Our input test cases might look like this:

| First Param | Operator | Second Param | HTTP Response | Result          |
|-------------|----------|--------------|---------------|-----------------|
| 7           | +        | 8            | 200           | 15              |
| 7           | x        | 8            | 200           | 56              |
| 7           | -        | 8            | 200           | -1              |
| 7           | /        | 8            | 200           | 0.875           |
| 7           | /        | 0            | 200           | ERROR: DIV ZERO |

RexSpecs will run that test against your service, and report any differences between the expected
and actual results. You can check the results visually in the output. We can easily see we made a mistake in our test,
because one of the results was most unexpected:

| First Param | Operator | Second Param | HTTP Response | Result                                             |
|-------------|----------|--------------|---------------|----------------------------------------------------|
| 7           | +        | 8            | 200           | 15                                                 |
| 7           | x        | 8            | 400           | `Expected [56] but was: [Unsupported operator: "x"]` |
| 7           | -        | 8            | 200           | -1                                                 |
| 7           | /        | 8            | 200           | 0.875                                              |
| 7           | /        | 0            | 200           | ERROR: DIV ZERO                                    |

