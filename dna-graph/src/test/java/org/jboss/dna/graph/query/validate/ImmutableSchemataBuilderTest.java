/*
 * JBoss DNA (http://www.jboss.org/dna)
 * See the COPYRIGHT.txt file distributed with this work for information
 * regarding copyright ownership.  Some portions may be licensed
 * to Red Hat, Inc. under one or more contributor license agreements.
 * See the AUTHORS.txt file in the distribution for a full listing of 
 * individual contributors.
 *
 * JBoss DNA is free software. Unless otherwise indicated, all code in JBoss DNA
 * is licensed to you under the terms of the GNU Lesser General Public License as
 * published by the Free Software Foundation; either version 2.1 of
 * the License, or (at your option) any later version.
 * 
 * JBoss DNA is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this software; if not, write to the Free
 * Software Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA
 * 02110-1301 USA, or see the FSF site: http://www.fsf.org.
 */
package org.jboss.dna.graph.query.validate;

import static org.hamcrest.core.Is.is;
import static org.hamcrest.core.IsInstanceOf.instanceOf;
import static org.hamcrest.core.IsNull.notNullValue;
import static org.junit.Assert.assertThat;
import org.jboss.dna.graph.ExecutionContext;
import org.jboss.dna.graph.property.PropertyType;
import org.jboss.dna.graph.query.model.SelectorName;
import org.jboss.dna.graph.query.validate.Schemata.Table;
import org.jboss.dna.graph.query.validate.Schemata.View;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

/**
 * 
 */
public class ImmutableSchemataBuilderTest {

    private ExecutionContext context;
    private ImmutableSchemata.Builder builder;
    private Schemata schemata;

    @Before
    public void beforeEach() {
        context = new ExecutionContext();
        builder = ImmutableSchemata.createBuilder(context);
    }

    @After
    public void afterEach() {
        context = null;
        builder = null;
        schemata = null;
    }

    @Test
    public void shouldBuildSchemaForSingleTableWithDefaultTypeForColumns() {
        builder.addTable("t1", "c1", "c2", "c3");
        schemata = builder.build();
        Table table = schemata.getTable(selector("t1"));
        assertThat(table, is(notNullValue()));
        assertThat(table.getColumn("c1").getPropertyType(), is(PropertyType.STRING));
        assertThat(table.getColumn("c2").getPropertyType(), is(PropertyType.STRING));
        assertThat(table.getColumn("c3").getPropertyType(), is(PropertyType.STRING));
    }

    @Test
    public void shouldBuildSchemaForMultipleTablesWithDefaultTypeForColumns() {
        builder.addTable("t1", "c11", "c12", "c13");
        builder.addTable("t2", "c21", "c22", "c23");
        schemata = builder.build();
        Table table = schemata.getTable(selector("t1"));
        assertThat(table, is(notNullValue()));
        assertThat(table.getColumn("c11").getPropertyType(), is(PropertyType.STRING));
        assertThat(table.getColumn("c12").getPropertyType(), is(PropertyType.STRING));
        assertThat(table.getColumn("c13").getPropertyType(), is(PropertyType.STRING));
        table = schemata.getTable(selector("t2"));
        assertThat(table, is(notNullValue()));
        assertThat(table.getColumn("c21").getPropertyType(), is(PropertyType.STRING));
        assertThat(table.getColumn("c22").getPropertyType(), is(PropertyType.STRING));
        assertThat(table.getColumn("c23").getPropertyType(), is(PropertyType.STRING));
    }

    @Test
    public void shouldBuildSchemaContainingViewOfSingleTable() {
        builder.addTable("t1", "c1", "c2", "c3");
        builder.addView("t2", "SELECT * FROM t1 WHERE c1=3");
        schemata = builder.build();
        Table table = schemata.getTable(selector("t1"));
        assertThat(table, is(notNullValue()));
        assertThat(table.getColumn("c1").getPropertyType(), is(PropertyType.STRING));
        assertThat(table.getColumn("c2").getPropertyType(), is(PropertyType.STRING));
        assertThat(table.getColumn("c3").getPropertyType(), is(PropertyType.STRING));
        table = schemata.getTable(selector("t2"));
        assertThat(table, is(instanceOf(View.class)));
        assertThat(table.getColumn("c1").getPropertyType(), is(PropertyType.STRING));
        assertThat(table.getColumn("c2").getPropertyType(), is(PropertyType.STRING));
        assertThat(table.getColumn("c3").getPropertyType(), is(PropertyType.STRING));
    }

    @Test
    public void shouldBuildSchemaContainingViewUsingAliasesOfColumns() {
        builder.addTable("t1", "c1", "c2", "c3");
        builder.addView("t2", "SELECT c1 as v1, c2 FROM t1 WHERE c1=3");
        schemata = builder.build();
        Table table = schemata.getTable(selector("t1"));
        assertThat(table, is(notNullValue()));
        assertThat(table.getColumn("c1").getPropertyType(), is(PropertyType.STRING));
        assertThat(table.getColumn("c2").getPropertyType(), is(PropertyType.STRING));
        assertThat(table.getColumn("c3").getPropertyType(), is(PropertyType.STRING));
        table = schemata.getTable(selector("t2"));
        assertThat(table, is(instanceOf(View.class)));
        assertThat(table.getColumn("v1").getPropertyType(), is(PropertyType.STRING));
        assertThat(table.getColumn("c2").getPropertyType(), is(PropertyType.STRING));
    }

    @Test
    public void shouldBuildSchemaContainingViewOfMultiTableJoin() {
        builder.addTable("t1", "c11", "c12", "c13");
        builder.addTable("t2", "c21", "c22", "c23");
        builder.addView("v1", "SELECT t1.c11 as x1, t1.c12, t2.c23 FROM t1 JOIN t2 ON t1.c11=t2.c21 WHERE t1.c11=3");
        schemata = builder.build();
        Table table = schemata.getTable(selector("t1"));
        assertThat(table, is(notNullValue()));
        assertThat(table.getColumn("c11").getPropertyType(), is(PropertyType.STRING));
        assertThat(table.getColumn("c12").getPropertyType(), is(PropertyType.STRING));
        assertThat(table.getColumn("c13").getPropertyType(), is(PropertyType.STRING));
        table = schemata.getTable(selector("t2"));
        assertThat(table, is(notNullValue()));
        assertThat(table.getColumn("c21").getPropertyType(), is(PropertyType.STRING));
        assertThat(table.getColumn("c22").getPropertyType(), is(PropertyType.STRING));
        assertThat(table.getColumn("c23").getPropertyType(), is(PropertyType.STRING));
        schemata = builder.build();
        table = schemata.getTable(selector("v1"));
        assertThat(table, is(instanceOf(View.class)));
        assertThat(table.getColumn("x1").getPropertyType(), is(PropertyType.STRING));
        assertThat(table.getColumn("c12").getPropertyType(), is(PropertyType.STRING));
        assertThat(table.getColumn("c23").getPropertyType(), is(PropertyType.STRING));
    }

    @Test
    public void shouldBuildSchemaContainingViewUsingView() {
        builder.addTable("t1", "c11", "c12", "c13");
        builder.addTable("t2", "c21", "c22", "c23");
        builder.addView("v2", "SELECT x1, c12 FROM v1 WHERE x1=3");
        builder.addView("v1", "SELECT t1.c11 as x1, t1.c12, t2.c23 FROM t1 JOIN t2 ON t1.c11=t2.c21 WHERE t1.c11=3");
        schemata = builder.build();
        Table table = schemata.getTable(selector("t1"));
        assertThat(table, is(notNullValue()));
        assertThat(table.getColumn("c11").getPropertyType(), is(PropertyType.STRING));
        assertThat(table.getColumn("c12").getPropertyType(), is(PropertyType.STRING));
        assertThat(table.getColumn("c13").getPropertyType(), is(PropertyType.STRING));
        table = schemata.getTable(selector("t2"));
        assertThat(table, is(notNullValue()));
        assertThat(table.getColumn("c21").getPropertyType(), is(PropertyType.STRING));
        assertThat(table.getColumn("c22").getPropertyType(), is(PropertyType.STRING));
        assertThat(table.getColumn("c23").getPropertyType(), is(PropertyType.STRING));
        schemata = builder.build();
        table = schemata.getTable(selector("v1"));
        assertThat(table, is(instanceOf(View.class)));
        assertThat(table.getColumn("x1").getPropertyType(), is(PropertyType.STRING));
        assertThat(table.getColumn("c12").getPropertyType(), is(PropertyType.STRING));
        assertThat(table.getColumn("c23").getPropertyType(), is(PropertyType.STRING));
        table = schemata.getTable(selector("v2"));
        assertThat(table, is(instanceOf(View.class)));
        assertThat(table.getColumn("x1").getPropertyType(), is(PropertyType.STRING));
        assertThat(table.getColumn("c12").getPropertyType(), is(PropertyType.STRING));
    }

    protected SelectorName selector( String name ) {
        return new SelectorName(name);
    }

}
