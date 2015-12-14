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

import java.util.List;
import java.util.Set;

import org.sonar.api.server.rule.RulesDefinition;
import org.sonar.check.Priority;
import org.sonar.check.Rule;
import org.sonar.plugins.plsqlopen.api.PlSqlGrammar;
import org.sonar.plugins.plsqlopen.api.symbols.Scope;
import org.sonar.plugins.plsqlopen.api.symbols.Symbol;
import org.sonar.squidbridge.annotations.ActivatedByDefault;
import org.sonar.squidbridge.annotations.SqaleConstantRemediation;
import org.sonar.squidbridge.annotations.SqaleSubCharacteristic;

import com.sonar.sslr.api.AstNode;

@Rule(
    key = UnusedVariableCheck.CHECK_KEY,
    priority = Priority.MAJOR,
    tags = Tags.UNUSED
)
@SqaleSubCharacteristic(RulesDefinition.SubCharacteristics.UNDERSTANDABILITY)
@SqaleConstantRemediation("2min")
@ActivatedByDefault
public class UnusedVariableCheck extends AbstractBaseCheck {

    public static final String CHECK_KEY = "UnusedVariable";

    @Override
    public void leaveFile(AstNode astNode) {
        Set<Scope> scopes = getPlSqlContext().getSymbolTable().getScopes();
        for (Scope scope : scopes) {
            if (scope.tree().isNot(PlSqlGrammar.CREATE_PACKAGE, PlSqlGrammar.FOR_STATEMENT)) {
                checkScope(scope);
            }
        }
    }
    
    private void checkScope(Scope scope) {
        List<Symbol> symbols = scope.getSymbols(Symbol.Kind.VARIABLE);
        for (Symbol symbol : symbols) {
            if (symbol.usages().isEmpty()) {
                getContext().createLineViolation(this, getLocalizedMessage(CHECK_KEY),
                        symbol.declaration(), symbol.declaration().getTokenOriginalValue());
            }
        }
    }

}