/**
 * This file is part of Craftconomy3.
 * <p>
 * Copyright (c) 2011-2016, Greatman <http://github.com/greatman/>
 * Copyright (c) 2016-2017, Aztorius <http://github.com/Aztorius/>
 * Copyright (c) 2018, Pavog <http://github.com/pavog/>
 * <p>
 * Craftconomy3 is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * <p>
 * Craftconomy3 is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 * <p>
 * You should have received a copy of the GNU Lesser General Public License
 * along with Craftconomy3.  If not, see <http://www.gnu.org/licenses/>.
 */
package com.greatmancode.craftconomy3.commands;

import com.greatmancode.craftconomy3.Common;
import com.greatmancode.craftconomy3.TestCommandSender;
import com.greatmancode.craftconomy3.TestInitializator;
import com.greatmancode.craftconomy3.commands.currency.*;
import com.greatmancode.craftconomy3.currency.Currency;
import com.greatmancode.tools.commands.PlayerCommandSender;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.util.UUID;

import static org.junit.Assert.*;

public class TestCurrencyCommands {

    private static PlayerCommandSender TEST_USER;

    @Before
    public void setUp() {
        new TestInitializator();
        TEST_USER = createTestUser("testuser41");
    }

    @After
    public void close() {
        Common.getInstance().onDisable();
    }

    @Test
    public void testAddCommand() {
        CurrencyAddCommand command = new CurrencyAddCommand("add");
        // Add a currency
        command.execute(TEST_USER, new String[]{"Pound", "Pounds", "Penny", "Pence", "£"});
        assertNotNull(Common.getInstance().getCurrencyManager().getCurrency("Pound"));

        // Check for correct values
        Currency pound = Common.getInstance().getCurrencyManager().getCurrency("Pound");
        assertEquals("Pound", pound.getName());
        assertEquals("Pounds", pound.getPlural());
        assertEquals("Penny", pound.getMinor());
        assertEquals("Pence", pound.getMinorPlural());
        assertEquals("£", pound.getSign());

        // Try to add the same currency again
        final int currencyCountBefore = Common.getInstance().getCurrencyManager().getCurrencyNames().size();
        command.execute(TEST_USER, new String[]{"Pound", "Pounds", "Penny", "Pence", "£"});
        final int currencyCountAfter = Common.getInstance().getCurrencyManager().getCurrencyNames().size();
        assertEquals(currencyCountBefore, currencyCountAfter);

        // Try to add a currency without a sign
        command.execute(TEST_USER, new String[]{"Euro", "Euro", "Cent", "Cent"});
        assertEquals(currencyCountBefore, currencyCountAfter);
    }

    @Test
    public void testDeleteCommand() {
        CurrencyDeleteCommand command = new CurrencyDeleteCommand("delete");

        // Add a new currency
        Common.getInstance().getCurrencyManager().addCurrency("Yen", "Yen", "Sen", "Sen", "¥", false);

        // Delete unknown currency
        final int currencyCountBefore = Common.getInstance().getCurrencyManager().getCurrencyNames().size();
        command.execute(TEST_USER, new String[]{"unknown"});
        final int currencyCountAfter = Common.getInstance().getCurrencyManager().getCurrencyNames().size();
        assertEquals(currencyCountBefore, currencyCountAfter);

        // Delete new currency
        assertNotNull(Common.getInstance().getCurrencyManager().getCurrency("Yen"));
        command.execute(TEST_USER, new String[]{"Yen"});
        assertNull(Common.getInstance().getCurrencyManager().getCurrency("Yen"));
    }

    @Test
    public void testDefaultCommand() {
        CurrencyDefaultCommand command = new CurrencyDefaultCommand("default");

        // Add a new default currency
        Common.getInstance().getCurrencyManager().addCurrency("Dollar", "Dollars", "Cent", "Cent", "$", true);
        Common.getInstance().getCurrencyManager().setDefault(Common.getInstance().getCurrencyManager().getCurrency("Dollar"));
        // Add another new currency
        Common.getInstance().getCurrencyManager().addCurrency("Euro", "Euro", "Cent", "Cent", "€", true);

        // Set new currency to default
        final String oldDefaultCurrency = Common.getInstance().getCurrencyManager().getDefaultCurrency().getName();
        command.execute(TEST_USER, new String[]{"Euro"});
        final String newDefaultCurrency = Common.getInstance().getCurrencyManager().getDefaultCurrency().getName();
        assertNotEquals(oldDefaultCurrency, newDefaultCurrency);
        assertEquals("Euro", newDefaultCurrency);

        // Test with unknown currency
        command.execute(TEST_USER, new String[]{"unknown"});
        assertEquals(newDefaultCurrency, Common.getInstance().getCurrencyManager().getDefaultCurrency().getName());
    }

    @Test
    public void testEditCommand() {
        CurrencyEditCommand command = new CurrencyEditCommand("edit");

        // Add a new currency
        Common.getInstance().getCurrencyManager().addCurrency("Yen", "Yen", "Sen", "Sen", "¥", false);

        // Try with unknown currency
        command.execute(TEST_USER, new String[]{"name", "unknown", "test"});
        command.execute(TEST_USER, new String[]{"nameplural", "unknown", "test"});
        command.execute(TEST_USER, new String[]{"minor", "unknown", "test"});
        command.execute(TEST_USER, new String[]{"minorplural", "unknown", "test"});
        command.execute(TEST_USER, new String[]{"sign", "unknown", "test"});

        // Try with unknown key
        command.execute(TEST_USER, new String[]{"unknown", "Yen", "test"});

        // Try without value
        command.execute(TEST_USER, new String[]{"name", "Yen", ""});
        command.execute(TEST_USER, new String[]{"nameplural", "Yen", ""});
        command.execute(TEST_USER, new String[]{"minor", "Yen", ""});
        command.execute(TEST_USER, new String[]{"minorplural", "Yen", ""});
        command.execute(TEST_USER, new String[]{"sign", "Yen", ""});

        // Check that yen still has the correct values after the tests
        Currency yen = Common.getInstance().getCurrencyManager().getCurrency("Yen");
        assertEquals("Yen", yen.getName());
        assertEquals("Yen", yen.getPlural());
        assertEquals("Sen", yen.getMinor());
        assertEquals("Sen", yen.getMinorPlural());
        assertEquals("¥", yen.getSign());


        // Test for success
        command.execute(TEST_USER, new String[]{"nameplural", "Yen", "Pounds"});
        assertEquals("Pounds", Common.getInstance().getCurrencyManager().getCurrency("Yen").getPlural());
        command.execute(TEST_USER, new String[]{"minor", "Yen", "Penny"});
        assertEquals("Penny", Common.getInstance().getCurrencyManager().getCurrency("Yen").getMinor());
        command.execute(TEST_USER, new String[]{"minorplural", "Yen", "Pence"});
        assertEquals("Pence", Common.getInstance().getCurrencyManager().getCurrency("Yen").getMinorPlural());
        command.execute(TEST_USER, new String[]{"sign", "Yen", "£"});
        assertEquals("£", Common.getInstance().getCurrencyManager().getCurrency("Yen").getSign());
        command.execute(TEST_USER, new String[]{"name", "Yen", "Pound"});
        assertEquals("Pound", Common.getInstance().getCurrencyManager().getCurrency("Pound").getName());
    }

    @Test
    public void testInfoCommand() {
        CurrencyInfoCommand command = new CurrencyInfoCommand("info");

        // Add a new currency
        Common.getInstance().getCurrencyManager().addCurrency("Yen", "Yen", "Sen", "Sen", "¥", false);
        command.execute(TEST_USER, new String[]{"Yen"});

        // Try with unknown currency
        command.execute(TEST_USER, new String[]{"unknown"});

        // Try without a currency
        command.execute(TEST_USER, new String[]{});
    }

    @Test
    public void testRatesCommand() {
        CurrencyRatesCommand command = new CurrencyRatesCommand("rates");
        // Add a new currency and execute the command
        Common.getInstance().getCurrencyManager().addCurrency("Yen", "Yen", "Sen", "Sen", "¥", false);
        command.execute(TEST_USER, new String[]{});
    }

    @Test
    public void testListCommand() {
        CurrencyListCommand command = new CurrencyListCommand("list");
        // Add a new currency and execute the command
        Common.getInstance().getCurrencyManager().addCurrency("Yen", "Yen", "Sen", "Sen", "¥", false);
        command.execute(TEST_USER, new String[]{});
    }

    private PlayerCommandSender createTestUser(String name) {
        UUID uuid = UUID.randomUUID();
        return new PlayerCommandSender<>(name, uuid, new TestCommandSender(uuid, name));
    }
}