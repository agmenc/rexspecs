package com.mycompany.fixture

import com.rexspecs.fixture.FixtureRegistry

class MyFixtureRegistry: FixtureRegistry(
    "Calculator" to Calculator()
)

