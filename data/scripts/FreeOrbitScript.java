package data.scripts;

import com.fs.starfarer.api.EveryFrameScript;
import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.campaign.CampaignFleetAPI;
import com.fs.starfarer.api.fleet.FleetMemberAPI;

import java.util.List;

public class FreeOrbitScript implements EveryFrameScript {

    private static final String STAT_ID = "free_orbit_no_supplies";
    private static final String MOD_ID = "free_orbit";
    private static final String SETTING_ID = "supplyReductionPercent";

    private boolean wasOrbiting = false;

    private float getReductionMultiplier() {
        if (Global.getSettings().getModManager().isModEnabled("lunalib")) {
            // Should crash if the setting is missing
            final int pct = lunalib.lunaSettings.LunaSettings.getInt(MOD_ID, SETTING_ID);
            return 1f - (pct / 100f);
        }
        return 0f;
    }

    public boolean isDone() {return false;}
    public boolean runWhilePaused() {return false;}

    @Override
    public void advance(float amount) {
        final CampaignFleetAPI fleet = Global.getSector().getPlayerFleet();
        if (fleet == null) return;

        final boolean isOrbiting = fleet.getOrbit() != null;

        if (isOrbiting) {
            applyFreeOrbit(fleet);
        } else if (wasOrbiting) {
            removeFreeOrbit(fleet);
        }

        wasOrbiting = isOrbiting;
    }

    private void applyFreeOrbit(CampaignFleetAPI fleet) {
        final float mult = getReductionMultiplier();
        final List<FleetMemberAPI> members = fleet.getFleetData().getMembersListCopy();
        for (FleetMemberAPI member : members) {
            member.getStats().getSuppliesPerMonth().modifyMult(STAT_ID, mult);
        }
    }

    private void removeFreeOrbit(CampaignFleetAPI fleet) {
        final List<FleetMemberAPI> members = fleet.getFleetData().getMembersListCopy();
        for (FleetMemberAPI member : members) {
            member.getStats().getSuppliesPerMonth().unmodify(STAT_ID);
        }
    }
}
