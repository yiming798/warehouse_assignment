package com.warehouse.gui.viewmodel;

import com.warehouse.models.consumable.Drink;
import com.warehouse.models.consumable.Food;
import com.warehouse.models.tool.Toolbox;
import com.warehouse.models.weapon.Gun;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.assertEquals;

class ItemRowViewTest {

    @Test
    void shouldShowExpiredAndConsumedStatusesCorrectly() {
        Food expiredFood = new Food("Soup", 1.0, LocalDate.now().minusDays(1));
        ItemRowView expiredRow = new ItemRowView(expiredFood);
        assertEquals("Expired", expiredRow.getStatus());
        assertEquals("-", expiredRow.getDurabilityDisplay());
        assertEquals("-", expiredRow.getBulletsDisplay());

        Drink drink = new Drink("Water", 1.0, LocalDate.now().plusDays(2));
        drink.setConsumed(true);
        ItemRowView consumedDrink = new ItemRowView(drink);
        assertEquals("Consumed (Drank)", consumedDrink.getStatus());
        assertEquals(String.valueOf(LocalDate.now().plusDays(2)), consumedDrink.getExpiration());
    }

    @Test
    void shouldDisplayDurabilityAndBulletsForWeaponsAndTools() {
        Gun gun = new Gun("Gun", 2.0, 7);
        gun.setDurability(80);
        ItemRowView gunRow = new ItemRowView(gun);
        assertEquals("Gun", gunRow.getType());
        assertEquals("7", gunRow.getBulletsDisplay());
        assertEquals("80", gunRow.getDurabilityDisplay());
        assertEquals(0.8, gunRow.getDurabilityProgress(), 1e-9);

        Toolbox toolbox = new Toolbox("Box", 1.0);
        toolbox.setDurability(55);
        ItemRowView toolboxRow = new ItemRowView(toolbox);
        assertEquals("55", toolboxRow.getDurabilityDisplay());
        assertEquals("-", toolboxRow.getBulletsDisplay());
    }
}


