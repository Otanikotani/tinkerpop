/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.apache.tinkerpop.gremlin.process.computer.traversal.step.map;

import org.apache.tinkerpop.gremlin.process.computer.ComputerResult;
import org.apache.tinkerpop.gremlin.process.computer.traversal.step.VertexComputing;
import org.apache.tinkerpop.gremlin.process.traversal.Step;
import org.apache.tinkerpop.gremlin.process.traversal.Traversal;
import org.apache.tinkerpop.gremlin.process.traversal.Traverser;
import org.apache.tinkerpop.gremlin.process.traversal.step.util.AbstractStep;
import org.apache.tinkerpop.gremlin.process.traversal.step.util.EmptyStep;
import org.apache.tinkerpop.gremlin.structure.Graph;

import java.util.NoSuchElementException;
import java.util.concurrent.ExecutionException;

/**
 * @author Marko A. Rodriguez (http://markorodriguez.com)
 */
public abstract class VertexProgramStep extends AbstractStep<ComputerResult, ComputerResult> implements VertexComputing {

    protected boolean first = true;

    public VertexProgramStep(final Traversal.Admin traversal) {
        super(traversal);
    }

    @Override
    protected Traverser<ComputerResult> processNextStart() throws NoSuchElementException {
        try {
            if (this.first && this.getPreviousStep() instanceof EmptyStep) {
                this.first = false;
                final Graph graph = this.getTraversal().getGraph().get();
                return this.getTraversal().getTraverserGenerator().generate(this.generateComputer(graph).program(this.generateProgram(graph)).submit().get(), this, 1l);
            } else {
                final Traverser.Admin<ComputerResult> traverser = this.starts.next();
                final Graph graph = traverser.get().graph();
                return traverser.split(this.generateComputer(graph).program(this.generateProgram(graph)).submit().get(), this);
            }
        } catch (final InterruptedException | ExecutionException e) {
            throw new IllegalStateException(e.getMessage(), e);
        }
    }

    protected boolean previousTraversalVertexProgram() {
        Step<?, ?> currentStep = this;
        while (!(currentStep instanceof EmptyStep)) {
            if (currentStep instanceof TraversalVertexProgramStep)
                return true;
            currentStep = currentStep.getPreviousStep();
        }
        return false;
    }
}
