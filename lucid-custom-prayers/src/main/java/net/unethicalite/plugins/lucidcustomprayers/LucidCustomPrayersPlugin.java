package net.unethicalite.plugins.lucidcustomprayers;

import com.google.inject.Provides;
import net.runelite.api.*;
import net.runelite.api.events.*;
import net.runelite.api.widgets.Widget;
import net.runelite.client.callback.ClientThread;
import net.runelite.client.config.ConfigManager;
import net.runelite.client.eventbus.Subscribe;
import net.runelite.client.events.ConfigChanged;
import net.runelite.client.plugins.Plugin;
import net.runelite.client.plugins.PluginDescriptor;
import net.unethicalite.api.utils.MessageUtils;
import net.unethicalite.api.widgets.Prayers;
import net.unethicalite.api.widgets.Widgets;
import org.pf4j.Extension;
import javax.inject.Inject;
import javax.inject.Singleton;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Extension
@PluginDescriptor(
        name = "Lucid Custom Prayers",
        description = "Set up auto prayers based on various event IDs",
        enabledByDefault = false,
        tags = {"prayer", "swap"}
)
@Singleton
public class LucidCustomPrayersPlugin extends Plugin
{

    @Inject
    private Client client;

    @Inject
    private ClientThread clientThread;

    @Inject
    private LucidCustomPrayersConfig config;

    private Map<EventType, List<CustomPrayer>> eventMap = new HashMap<>();

    private List<ScheduledPrayer> scheduledPrayers = new ArrayList<>();

    private int lastAnimationChanged = 0;

    private int lastNpcSpawned = 0;

    private int lastNpcDespawned = 0;

    private int lastNpcChanged = 0;

    private int lastProjectileSpawned = 0;

    private int lastGraphicsCreated = 0;

    private int lastGameObjectSpawned = 0;



    @Provides
    LucidCustomPrayersConfig getConfig(final ConfigManager configManager)
    {
        return configManager.getConfig(LucidCustomPrayersConfig.class);
    }

    @Override
    protected void startUp()
    {
        parsePrayers();
    }

    @Override
    protected void shutDown()
    {

    }

    @Subscribe
    private void onAnimationChanged(final AnimationChanged event)
    {
        if (event.getActor() == null)
        {
            return;
        }

        if (lastAnimationChanged != client.getTickCount())
        {
            eventFired(EventType.ANIMATION_CHANGED, event.getActor().getAnimation());
            lastAnimationChanged = client.getTickCount();
        }
    }

    @Subscribe
    private void onNpcSpawned(final NpcSpawned event)
    {
        if (event.getNpc() == null)
        {
            return;
        }

        if (lastNpcSpawned != client.getTickCount())
        {
            eventFired(EventType.NPC_SPAWNED, event.getNpc().getId());
            lastNpcSpawned = client.getTickCount();
        }
    }

    @Subscribe
    private void onNpcDespawned(final NpcDespawned event)
    {
        if (event.getNpc() == null)
        {
            return;
        }

        if (lastNpcDespawned != client.getTickCount())
        {
            eventFired(EventType.NPC_DESPAWNED, event.getNpc().getId());
            lastNpcDespawned = client.getTickCount();
        }
    }

    @Subscribe
    private void onNpcChanged(final NpcChanged event)
    {
        if (event.getNpc() == null)
        {
            return;
        }

        if (lastNpcChanged != client.getTickCount())
        {
            eventFired(EventType.NPC_CHANGED, event.getOld().getId());
            lastNpcChanged = client.getTickCount();
        }
    }

    @Subscribe
    private void onProjectile(final ProjectileSpawned event)
    {
        if (lastProjectileSpawned != client.getTickCount())
        {
            eventFired(EventType.PROJECTILE_SPAWNED, event.getProjectile().getId());
            lastProjectileSpawned = client.getTickCount();
        }
    }

    @Subscribe
    private void onGraphics(final GraphicsObjectCreated event)
    {
        if (lastGraphicsCreated != client.getTickCount())
        {
            eventFired(EventType.GRAPHICS_CREATED, event.getGraphicsObject().getId());
            lastGraphicsCreated = client.getTickCount();
        }
    }

    @Subscribe
    private void onGameObjectSpawned(final GameObjectSpawned event)
    {
        if (lastGameObjectSpawned != client.getTickCount())
        {
            eventFired(EventType.GAME_OBJECT_SPAWNED, event.getGameObject().getId());
            lastGameObjectSpawned = client.getTickCount();
        }
    }

    @Subscribe
    private void onConfigChanged(final ConfigChanged event)
    {
        if (!event.getGroup().equals("lucid-custom-prayers"))
        {
            return;
        }

        parsePrayers();
    }

    @Subscribe
    private void onGameTick(final GameTick event)
    {
        for (ScheduledPrayer prayer : scheduledPrayers)
        {
            if (client.getTickCount() == prayer.getActivationTick())
            {
                activatePrayer(prayer.getPrayer(), prayer.isToggle());
            }
        }

        scheduledPrayers.removeIf(prayer -> prayer.getActivationTick() <= client.getTickCount() - 1);
    }

    private void parsePrayers()
    {
        eventMap.clear();

        for (int i = 1; i < 11; i++)
        {
            parsePrayerSlot(i);
        }
    }

    private void parsePrayerSlot(int id)
    {
        List<Integer> ids = List.of();
        List<Integer> delays = List.of();
        Prayer prayChoice = null;
        EventType type = EventType.ANIMATION_CHANGED;
        boolean toggle = false;

        switch (id)
        {
            case 1:
                if (config.activated1())
                {
                    ids = intListFromString(config.pray1Ids());
                    delays = intListFromString(config.pray1delays());
                    prayChoice = config.pray1choice();
                    type = config.eventType1();
                    toggle = config.toggle1();
                }
                break;
            case 2:
                if (config.activated2())
                {
                    ids = intListFromString(config.pray2Ids());
                    delays = intListFromString(config.pray2delays());
                    prayChoice = config.pray2choice();
                    type = config.eventType2();
                    toggle = config.toggle2();
                }
                break;
            case 3:
                if (config.activated3())
                {
                    ids = intListFromString(config.pray3Ids());
                    delays = intListFromString(config.pray3delays());
                    prayChoice = config.pray3choice();
                    type = config.eventType3();
                    toggle = config.toggle3();
                }
                break;
            case 4:
                if (config.activated4())
                {
                    ids = intListFromString(config.pray4Ids());
                    delays = intListFromString(config.pray4delays());
                    prayChoice = config.pray4choice();
                    type = config.eventType4();
                    toggle = config.toggle4();
                }
                break;
            case 5:
                if (config.activated5())
                {
                    ids = intListFromString(config.pray5Ids());
                    delays = intListFromString(config.pray5delays());
                    prayChoice = config.pray5choice();
                    type = config.eventType5();
                    toggle = config.toggle5();
                }
                break;
            case 6:
                if (config.activated6())
                {
                    ids = intListFromString(config.pray6Ids());
                    delays = intListFromString(config.pray6delays());
                    prayChoice = config.pray6choice();
                    type = config.eventType6();
                    toggle = config.toggle6();
                }
                break;
            case 7:
                if (config.activated7())
                {
                    ids = intListFromString(config.pray7Ids());
                    delays = intListFromString(config.pray7delays());
                    prayChoice = config.pray7choice();
                    type = config.eventType7();
                    toggle = config.toggle7();
                }
                break;
            case 8:
                if (config.activated8())
                {
                    ids = intListFromString(config.pray8Ids());
                    delays = intListFromString(config.pray8delays());
                    prayChoice = config.pray8choice();
                    type = config.eventType8();
                    toggle = config.toggle8();
                }
                break;
            case 9:
                if (config.activated9())
                {
                    ids = intListFromString(config.pray9Ids());
                    delays = intListFromString(config.pray9delays());
                    prayChoice = config.pray9choice();
                    type = config.eventType9();
                    toggle = config.toggle9();
                }
                break;
            case 10:
                if (config.activated10())
                {
                    ids = intListFromString(config.pray10Ids());
                    delays = intListFromString(config.pray10delays());
                    prayChoice = config.pray10choice();
                    type = config.eventType10();
                    toggle = config.toggle10();
                }
                break;
        }

        if (ids.isEmpty() || prayChoice == null)
        {
            return;
        }

        populatePrayersList(ids, delays, prayChoice, type, toggle);
    }

    private List<Integer> intListFromString(String stringList)
    {
        List<Integer> ints = new ArrayList<>();
        if (stringList == null || stringList.trim().equals(""))
        {
            return ints;
        }

        if (stringList.contains(","))
        {
            String[] intStrings = stringList.trim().split(",");
            for (String s : intStrings)
            {
                try
                {
                    int anInt = Integer.parseInt(s);
                    ints.add(anInt);
                }
                catch (NumberFormatException e)
                {
                }
            }
        }
        else
        {
            try
            {
                int anInt = Integer.parseInt(stringList);
                ints.add(anInt);
            }
            catch (NumberFormatException e)
            {
            }
        }

        return ints;
    }

    private void populatePrayersList(List<Integer> ids, List<Integer> delays, Prayer prayer, EventType type, boolean toggle)
    {
        if (!delays.isEmpty() && delays.size() != ids.size())
        {
            if (client.getGameState() == GameState.LOGGED_IN)
            {
                MessageUtils.addMessage("If delays are specified, delays and ids list must be the same length!");
            }
            delays.clear();
        }

        List<CustomPrayer> prayerList = eventMap.get(type);

        if (prayerList == null)
        {
            prayerList = new ArrayList<>();
        }

        for (int i = 0; i < ids.size(); i++)
        {
            if (!delays.isEmpty())
            {
                prayerList.add(new CustomPrayer(ids.get(i), prayer, delays.get(i), toggle));
            }
            else
            {
                prayerList.add(new CustomPrayer(ids.get(i), prayer, 0, toggle));
            }
        }

        eventMap.put(type, prayerList);
    }

    private static void activatePrayer(Prayer prayer, boolean toggle)
    {
        if (Prayers.getPoints() == 0)
        {
            return;
        }

        if (Prayers.isEnabled(prayer) && !toggle)
        {
            return;
        }

        Widget widget = Widgets.get(prayer.getWidgetInfo());
        if (widget != null)
        {
            widget.interact(0);
        }
    }

    private void eventFired(EventType type, int id)
    {
        List<CustomPrayer> prayers = eventMap.get(type);
        if (prayers == null || prayers.isEmpty())
        {
            return;
        }

        for (CustomPrayer prayer : prayers)
        {
            if (prayer.getActivationId() == id)
            {
                scheduledPrayers.add(new ScheduledPrayer(prayer.getPrayerToActivate(), client.getTickCount() + prayer.getTickDelay(), prayer.isToggle()));
            }
        }
    }

}