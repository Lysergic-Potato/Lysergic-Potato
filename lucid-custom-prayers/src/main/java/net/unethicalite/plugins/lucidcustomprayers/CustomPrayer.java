package net.unethicalite.plugins.lucidcustomprayers;

import lombok.AllArgsConstructor;
import lombok.Data;
import net.runelite.api.Prayer;

@Data
@AllArgsConstructor
public class CustomPrayer
{

    private int activationId;

    private Prayer prayerToActivate;

    private int tickDelay;

    private boolean toggle;
}
