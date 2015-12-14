/*
 * Sonar PL/SQL Plugin (Community)
 * Copyright (C) 2015 Felipe Zorzo
 * felipe.b.zorzo@gmail.com
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 3 of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this program; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02
 */
package org.sonar.plsqlopen.checks;

import java.util.Deque;

import org.sonar.api.server.rule.RulesDefinition;
import org.sonar.check.Priority;
import org.sonar.check.Rule;
import org.sonar.plugins.plsqlopen.api.PlSqlGrammar;
import org.sonar.plugins.plsqlopen.api.symbols.Scope;
import org.sonar.plugins.plsqlopen.api.symbols.Symbol;
import org.sonar.squidbridge.annotations.ActivatedByDefault;
import org.sonar.squidbridge.annotations.SqaleConstantRemediation;
import org.sonar.squidbridge.annotations.SqaleSubCharacteristic;

import com.google.common.collect.ImmutableList;
import com.sonar.sslr.api.AstNode;

@Rule(
    key = VariableHidingCheck.CHECK_KEY,
    priority = Priority.MAJOR
)
@SqaleSubCharacteristic(RulesDefinition.SubCharacteristics.READABILITY)
@SqaleConstantRemediation("5min")
@ActivatedByDefault 
public class VariableHidingCheck extends AbstractBaseCheck {
    public static final String CHECK_KEY = "VariableHiding";

    @Override
    public void init() {
        subscribeTo(PlSqlGrammar.VARIABLE_DECLARATION);
    }
    
    @Override
    public void visitNode(AstNode node) {
        AstNode identifier = node.getFirstChild(PlSqlGrammar.IDENTIFIER_NAME);
        String name = identifier.getTokenOriginalValue();
        
        Scope scope = getPlSqlContext().getSymbolTable().getScopeForSymbol(identifier);
        if (scope != null) {
            Deque<Symbol> symbols = scope.getSymbolsAcessibleInScope(name);
            
            if (symbols.size() > 1) {
                AstNode originalVariable = symbols.getLast().declaration();
                
                if (!originalVariable.equals(identifier)) {
                    getPlSqlContext().createViolation(this, getLocalizedMessage(CHECK_KEY),
                            identifier,
                            ImmutableList.of(newLocation("Original", originalVariable)),
                            name,
                            originalVariable.getTokenLine());
                }
            }
        }
    }
}