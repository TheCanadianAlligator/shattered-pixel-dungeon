package com.shatteredpixel.shatteredpixeldungeon.items.wands;

import com.shatteredpixel.shatteredpixeldungeon.Assets;
import com.shatteredpixel.shatteredpixeldungeon.Dungeon;
import com.shatteredpixel.shatteredpixeldungeon.DungeonTilemap;
import com.shatteredpixel.shatteredpixeldungeon.actors.Actor;
import com.shatteredpixel.shatteredpixeldungeon.actors.Char;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.Blindness;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.Buff;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.Light;
import com.shatteredpixel.shatteredpixeldungeon.actors.mobs.*;
import com.shatteredpixel.shatteredpixeldungeon.actors.mobs.npcs.Ghost;
import com.shatteredpixel.shatteredpixeldungeon.effects.Beam;
import com.shatteredpixel.shatteredpixeldungeon.effects.CellEmitter;
import com.shatteredpixel.shatteredpixeldungeon.effects.Speck;
import com.shatteredpixel.shatteredpixeldungeon.effects.particles.RainbowParticle;
import com.shatteredpixel.shatteredpixeldungeon.effects.particles.ShadowParticle;
import com.shatteredpixel.shatteredpixeldungeon.items.scrolls.ScrollOfMagicMapping;
import com.shatteredpixel.shatteredpixeldungeon.items.weapon.enchantments.Paralysis;
import com.shatteredpixel.shatteredpixeldungeon.items.weapon.melee.MagesStaff;
import com.shatteredpixel.shatteredpixeldungeon.levels.Level;
import com.shatteredpixel.shatteredpixeldungeon.levels.Terrain;
import com.shatteredpixel.shatteredpixeldungeon.mechanics.Ballistica;
import com.shatteredpixel.shatteredpixeldungeon.scenes.GameScene;
import com.shatteredpixel.shatteredpixeldungeon.sprites.ItemSpriteSheet;
import com.watabou.noosa.audio.Sample;
import com.watabou.utils.Callback;
import com.watabou.utils.Random;

import java.util.Arrays;
import java.util.HashSet;

/**
 * Created by Evan on 10/04/2015.
 */
public class WandOfPrismaticLight extends Wand {

    {
        name = "Wand of Prismatic Light";
        //TODO: final sprite
        image = ItemSpriteSheet.WAND_PRISMATIC_LIGHT;

        collisionProperties = Ballistica.MAGIC_BOLT;
    }

    //FIXME: this is sloppy
    private static HashSet<Class> evilMobs = new HashSet<Class>(Arrays.asList(
            //Any Location
            Mimic.class, Wraith.class,
            //Sewers
            Ghost.FetidRat.class,
            Goo.class,
            //Prison
            Skeleton.class , Thief.class, Bandit.class,
            //Caves

            //City
            Warlock.class, Monk.class, Senior.class,
            King.class, King.Undead.class,
            //Halls
            Succubus.class, Eye.class, Scorpio.class, Acidic.class,
            Yog.class, Yog.RottingFist.class, Yog.BurningFist.class, Yog.Larva.class
    ));

    @Override
    protected void onZap(Ballistica beam) {
        Char ch = Actor.findChar(beam.collisionPos);
        if (ch != null){
           affectTarget(ch);
        }
        affectMap(beam);

        if (curUser.viewDistance < 4)
            Buff.prolong( curUser, Light.class, 10f+level*5);
    }

    private void affectTarget(Char ch){
        //TODO: final balancing
        int dmg = Random.NormalIntRange(level, (int) (8+(level*(level/5f))));

        //two in (5+lvl) chance of failing
        if (Random.Int(5+level) >= 2) {
            Buff.prolong(ch, Blindness.class, 2f + (level * 0.5f));
            ch.sprite.emitter().burst(Speck.factory(Speck.LIGHT), 6 );
        }

        if (evilMobs.contains(ch.getClass())){
            ch.sprite.emitter().start( ShadowParticle.UP, 0.05f, 10+level );
            Sample.INSTANCE.play(Assets.SND_BURNING);

            ch.damage((int)(dmg*1.5), this);
        } else {
            ch.sprite.centerEmitter().burst( RainbowParticle.BURST, 10+level );

            ch.damage(dmg, this);
        }

    }

    private void affectMap(Ballistica beam){
        boolean noticed = false;
        for (int c: beam.subPath(0, beam.dist)){
            for (int n : Level.NEIGHBOURS9DIST2){
                int cell = c+n;
                if (!Level.insideMap(cell))
                    continue;

                if (Level.discoverable[cell])
                    Dungeon.level.mapped[cell] = true;

                int terr = Dungeon.level.map[cell];
                if ((Terrain.flags[terr] & Terrain.SECRET) != 0) {

                    Level.set( cell, Terrain.discover( terr ) );
                    GameScene.updateMap(cell);

                    GameScene.discoverTile( cell, terr );
                    ScrollOfMagicMapping.discover(cell);

                    noticed = true;
                }
            }

            CellEmitter.center(c).burst( RainbowParticle.BURST, Random.IntRange( 1, 2 ) );
        }
        if (noticed)
            Sample.INSTANCE.play( Assets.SND_SECRET );

        Dungeon.observe();
    }

    @Override
    protected void fx( Ballistica beam, Callback callback ) {
        curUser.sprite.parent.add(
                new Beam.LightRay(curUser.sprite.center(), DungeonTilemap.tileCenterToWorld(beam.collisionPos)));
        callback.call();
    }

    @Override
    public void onHit(MagesStaff staff, Char attacker, Char defender, int damage) {
        //acts like stunning enchant
        new Paralysis().proc(staff, attacker, defender, damage);
    }

    @Override
    public String desc() {
        return super.desc();
    }
}