package cz.craftmania.craftkeeper.sql;

import com.zaxxer.hikari.HikariDataSource;
import cz.craftmania.craftkeeper.Main;
import cz.craftmania.craftkeeper.objects.Multiplier;
import cz.craftmania.craftkeeper.objects.MultiplierType;
import cz.craftmania.craftkeeper.utils.Logger;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class SQLManager {

    private final Main plugin;
    private final ConnectionPoolManager pool;
    private HikariDataSource dataSource;

    public SQLManager(Main plugin) {
        this.plugin = plugin;
        pool = new ConnectionPoolManager(plugin);
    }

    public void onDisable() {
        pool.closePool();
    }

    public ConnectionPoolManager getPool() {
        return pool;
    }

    public final boolean tableMultiplierExists() {
        Connection conn = null;
        PreparedStatement ps = null;
        try {
            conn = pool.getConnection();
            ps = conn.prepareStatement("SELECT target FROM multipliers");
            ps.executeQuery();
            return ps.getResultSet().next();
        } catch (Exception ignored) {
            return false;
        } finally {
            pool.close(conn, ps, null);
        }
    }

    public final void createMultipliersTable() {
        new BukkitRunnable() {
            @Override
            public void run() {
                Connection conn = null;
                PreparedStatement ps = null;
                try {
                    conn = pool.getConnection();
                    ps = conn.prepareStatement(
                            "CREATE TABLE `multipliers` (" +
                                    " `id` int(255) NOT NULL AUTO_INCREMENT," +
                                    " `multiplier_type` varchar(64) COLLATE latin2_czech_cs NOT NULL," +
                                    " `target` varchar(64) COLLATE latin2_czech_cs NOT NULL," +
                                    " `target_uuid` varchar(64) COLLATE latin2_czech_cs," +
                                    " `length` BIGINT(255) NOT NULL," +
                                    " `remaining_length` BIGINT(255) NOT NULL," +
                                    " `percentage_boost` DOUBLE NOT NULL," +
                                    " `internal_id` BIGINT(255) NOT NULL," +
                                    " PRIMARY KEY (`id`)" +
                                    ") ENGINE=InnoDB DEFAULT CHARSET=latin2 COLLATE=latin2_czech_cs");
                    ps.execute();
                } catch (Exception ignored) {
                } finally {
                    pool.close(conn, ps, null);
                }
            }
        }.runTaskAsynchronously(Main.getInstance());
    }

    public final void createMultiplier(Multiplier multiplier) {
        new BukkitRunnable() {
            @Override
            public void run() {
                Connection conn = null;
                PreparedStatement ps = null;

                String UUIDString = "";
                if (multiplier.getTargetUUID() == null)
                    UUIDString = null;
                else
                    UUIDString = multiplier.getTargetUUID().toString();

                try {
                    conn = pool.getConnection();
                    ps = conn.prepareStatement("INSERT INTO multipliers (multiplier_type, target, target_uuid, length, remaining_length, percentage_boost, internal_id) VALUES (?,?,?,?,?,?,?);");
                    ps.setString(1, multiplier.getType().name());
                    ps.setString(2, multiplier.getTarget());
                    ps.setString(3, UUIDString);
                    ps.setLong(4, multiplier.getLength());
                    ps.setLong(5, multiplier.getRemainingLength());
                    ps.setDouble(6, multiplier.getPercentageBoost());
                    ps.setLong(7, multiplier.getInternalID());
                    ps.executeUpdate();
                } catch (Exception e) {
                    Logger.danger("Chyba při vytváření Multiplieru pro hráče " + multiplier.getTarget() + "!");
                    Main.getInstance().sendSentryException(e);
                    e.printStackTrace();
                } finally {
                    pool.close(conn, ps, null);
                }
            }
        }.runTaskAsynchronously(Main.getInstance());
    }

    public final List<Multiplier> getPlayersMultipliers(final Player p) {
        List<Multiplier> multipliers = new ArrayList<>();
        Connection conn = null;
        PreparedStatement ps = null;
        try {
            conn = pool.getConnection();
            ps = conn.prepareStatement("SELECT * FROM multipliers WHERE multipliers.target = ? AND multipliers.multiplier_type = ?");
            ps.setString(1, p.getName());
            ps.setString(2, "PERSONAL");
            ps.executeQuery();
            while (ps.getResultSet().next()) {
                String multiplier_type = ps.getResultSet().getString("multiplier_type");
                String target = ps.getResultSet().getString("target");
                String target_uuid_string = ps.getResultSet().getString("target_uuid");
                UUID target_uuid = null;
                if (target_uuid_string != null) {
                    target_uuid = UUID.fromString(target_uuid_string);
                }
                long length = ps.getResultSet().getLong("length");
                long remaining_length = ps.getResultSet().getLong("remaining_length");
                double percentage_boost = ps.getResultSet().getDouble("percentage_boost");
                long internal_id = ps.getResultSet().getLong("internal_id");
                Multiplier multiplier = new Multiplier(MultiplierType.getByName(multiplier_type), target, target_uuid, length, remaining_length, percentage_boost, internal_id);
                multipliers.add(multiplier);
            }
        } catch (Exception e) {
            Main.getInstance().sendSentryException(e);
            e.printStackTrace();
            Logger.danger("Chyba při získávání Multiplieru z SQL!");
        } finally {
            pool.close(conn, ps, null);
        }
        return multipliers;
    }

    public final List<Multiplier> getGlobalMultipliers() {
        List<Multiplier> multipliers = new ArrayList<>();
        Connection conn = null;
        PreparedStatement ps = null;
        try {
            conn = pool.getConnection();
            ps = conn.prepareStatement("SELECT * FROM multipliers WHERE multipliers.multiplier_type = ? OR multipliers.multiplier_type = ?");
            ps.setString(1, "GLOBAL");
            ps.setString(2, "EVENT");
            ps.executeQuery();
            while (ps.getResultSet().next()) {
                String multiplier_type = ps.getResultSet().getString("multiplier_type");
                String target = ps.getResultSet().getString("target");
                String target_uuid_string = ps.getResultSet().getString("target_uuid");
                UUID target_uuid = null;
                if (target_uuid_string != null) {
                    target_uuid = UUID.fromString(target_uuid_string);
                }
                long length = ps.getResultSet().getLong("length");
                long remaining_length = ps.getResultSet().getLong("remaining_length");
                double percentage_boost = ps.getResultSet().getDouble("percentage_boost");
                long internal_id = ps.getResultSet().getLong("internal_id");
                Multiplier multiplier = new Multiplier(MultiplierType.getByName(multiplier_type), target, target_uuid, length, remaining_length, percentage_boost, internal_id);
                multipliers.add(multiplier);
            }
        } catch (Exception e) {
            Main.getInstance().sendSentryException(e);
            e.printStackTrace();
            Logger.danger("Chyba při získávání Multiplieru z SQL!");
        } finally {
            pool.close(conn, ps, null);
        }
        return multipliers;
    }

    public final void updateMultiplierRemainingLength(Multiplier multiplier) {
        new BukkitRunnable() {
            @Override
            public void run() {
                if (multiplier.getRemainingLength() <= 0) {
                    removeMultiplier(multiplier);
                    return;
                }
                Connection conn = null;
                PreparedStatement ps = null;
                try {
                    conn = pool.getConnection();
                    ps = conn.prepareStatement("UPDATE multipliers SET remaining_length = ? WHERE internal_id = ?");
                    ps.setLong(1, multiplier.getRemainingLength());
                    ps.setLong(2, multiplier.getInternalID());
                    ps.executeUpdate();
                } catch (Exception ignored) {
                    ignored.printStackTrace();
                } finally {
                    pool.close(conn, ps, null);
                }
            }
        }.runTaskAsynchronously(Main.getInstance());
    }

    public final void updateListOfMultipliersRemainingLength(List<Multiplier> multipliers) {
        new BukkitRunnable() {
            @Override
            public void run() {
                Connection conn = null;
                PreparedStatement ps = null;
                try {
                    conn = pool.getConnection();
                    ps = conn.prepareStatement("UPDATE multipliers SET remaining_length = -1 WHERE internal_id = -1"); //Aby SQL neberečelo, že má prázdný statement
                    for (Multiplier multiplierInList : multipliers) {
                        if (multiplierInList.getRemainingLength() <= 0) {
                            removeMultiplier(multiplierInList);
                            continue;
                        }
                        ps.addBatch("UPDATE multipliers SET remaining_length = " + multiplierInList.getRemainingLength() + " WHERE internal_id = " + multiplierInList.getInternalID() + ";");
                    }
                    ps.executeLargeBatch();
                } catch (Exception exception) {
                    exception.printStackTrace();
                    Main.getInstance().sendSentryException(exception);
                    Logger.danger("Chyba při ukládání listu Multiplierů na SQL!");
                } finally {
                    pool.close(conn, ps, null);
                }
            }
        }.runTaskAsynchronously(Main.getInstance());
    }

    public final void removeMultiplier(Multiplier multiplier) {
        new BukkitRunnable() {
            @Override
            public void run() {
                Connection conn = null;
                PreparedStatement ps = null;
                try {
                    conn = pool.getConnection();
                    ps = conn.prepareStatement("DELETE FROM multipliers WHERE internal_id = ?");
                    ps.setLong(1, multiplier.getInternalID());
                    ps.executeUpdate();
                } catch (Exception ignored) {
                } finally {
                    pool.close(conn, ps, null);
                }
            }
        }.runTaskAsynchronously(Main.getInstance());
    }

    /**
     * Will return selected player settings.
     *
     * @param p        Player object
     * @param settings Settings name
     * @return Selected settings value
     */
    public final int getSettings(final Player p, final String settings) {
        Connection conn = null;
        PreparedStatement ps = null;
        try {
            conn = pool.getConnection();
            ps = conn.prepareStatement("SELECT " + settings + " FROM minigames.player_settings WHERE nick = '" + p.getName() + "'");
            ps.executeQuery();
            if (ps.getResultSet().next()) {
                return ps.getResultSet().getInt(settings);
            }
        } catch (Exception e) {
            Main.getInstance().sendSentryException(e);
            e.printStackTrace();
        } finally {
            pool.close(conn, ps, null);
        }
        return -1;
    }
}
