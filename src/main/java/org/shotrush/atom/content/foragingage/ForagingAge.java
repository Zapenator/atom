package org.shotrush.atom.content.foragingage;

import net.kyori.adventure.text.format.TextColor;
import org.shotrush.atom.core.age.Age;
import org.shotrush.atom.core.age.AgeManager;

public class ForagingAge {
    
    public static Age create() {
        return Age.builder()
                .id("foraging_age")
                .displayName("Foraging")
                .year(50000)
                .isBC(true)
                .order(0)
                .titleColor(TextColor.color(34, 139, 34))
                .description("Gathering resources from nature")
                .build();
    }
    
    public static void register(AgeManager manager) {
        Age foragingAge = create();
        manager.registerAges(foragingAge);
        manager.setAge(foragingAge);
    }
}
