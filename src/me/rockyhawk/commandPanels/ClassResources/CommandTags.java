package me.rockyhawk.commandPanels.ClassResources;

import com.google.common.io.ByteArrayDataOutput;
import com.google.common.io.ByteStreams;
import me.realized.tokenmanager.api.TokenManager;
import me.rockyhawk.commandPanels.commandpanels;
import org.bukkit.*;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.PotionMeta;
import org.bukkit.scheduler.BukkitRunnable;

import java.io.File;
import java.util.Objects;

public class CommandTags {
    commandpanels plugin;
    public CommandTags(commandpanels pl) {
        this.plugin = pl;
    }

    public void commandTags(Player p, String command) {
        String tag = plugin.config.getString("config.format.tag") + " ";
        //set cp placeholders
        command = plugin.papi(p, plugin.setCpPlaceholders(p, command));
        if (command.split("\\s")[0].equalsIgnoreCase("server=")) {
            //this contacts bungee and tells it to send the server change command
            ByteArrayDataOutput out = ByteStreams.newDataOutput();
            out.writeUTF("Connect");
            out.writeUTF(command.split("\\s")[1]);
            Player player = Bukkit.getPlayerExact(p.getName());
            assert player != null;
            player.sendPluginMessage(plugin, "BungeeCord", out.toByteArray());
        } else if (command.split("\\s")[0].equalsIgnoreCase("open=")) {
            //if player uses open= it will open the panel forced
            String panelName = command.split("\\s")[1];
            for(String[] tempName : plugin.panelNames){
                if(tempName[0].equals(panelName)){
                    YamlConfiguration panelConfig = YamlConfiguration.loadConfiguration(new File(plugin.panelsf + File.separator + plugin.panelFiles.get(Integer.parseInt(tempName[1]))));
                    plugin.openGui(panelName, p, panelConfig, 1, 0);
                    return;
                }
            }
        }else if (command.split("\\s")[0].equalsIgnoreCase("op=")) {
            //if player uses op= it will perform command as op
            boolean isop = p.isOp();
            try {
                p.setOp(true);
                Bukkit.dispatchCommand(p,plugin.papi(p, command.replace("op=", "").trim()));
                p.setOp(isop);
            } catch (Exception exc) {
                p.setOp(isop);
                plugin.debug(exc);
                p.sendMessage(tag + plugin.papi( plugin.config.getString("config.format.error") + " op=: Error in op command!"));
            }
        }else if (command.split("\\s")[0].equalsIgnoreCase("delay=")) {
            //if player uses op= it will perform command as op
            final int delaySeconds = Integer.parseInt(command.split("\\s")[1]);
            String finalCommand = command.split("\\s",3)[2];
            new BukkitRunnable() {
                @Override
                public void run() {
                    commandTags(p, finalCommand);
                    this.cancel();
                }
            }.runTaskTimer(plugin, 20*delaySeconds, 20); //20 ticks == 1 second
        } else if (command.split("\\s")[0].equalsIgnoreCase("console=")) {
            //if player uses console= it will perform command in the console
            Bukkit.dispatchCommand(Bukkit.getConsoleSender(), plugin.papi(p, command.replace("console=", "").trim()));
        } else if (command.split("\\s")[0].equalsIgnoreCase("buy=")) {
            //if player uses buy= it will be eg. buy= <price> <item> <amount of item> <ID>
            try {
                if (plugin.econ != null) {
                    if (plugin.econ.getBalance(p) >= Double.parseDouble(command.split("\\s")[1])) {
                        plugin.econ.withdrawPlayer(p, Double.parseDouble(command.split("\\s")[1]));
                        //if the message is empty don't send
                        if(!Objects.requireNonNull(plugin.config.getString("config.format.bought")).isEmpty()){
                            p.sendMessage(plugin.papi( tag + Objects.requireNonNull(plugin.config.getString("config.format.bought")).replaceAll("%cp-args%", command.split("\\s")[1])));
                        }
                        if (p.getInventory().firstEmpty() >= 0) {
                            p.getInventory().addItem(new ItemStack(Objects.requireNonNull(Material.matchMaterial(command.split("\\s")[2])), Integer.parseInt(command.split("\\s")[3])));
                        } else {
                            Objects.requireNonNull(p.getLocation().getWorld()).dropItemNaturally(p.getLocation(), new ItemStack(Objects.requireNonNull(Material.matchMaterial(command.split("\\s")[2])), Integer.parseInt(command.split("\\s")[3])));
                        }
                    } else {
                        p.sendMessage(plugin.papi( tag + plugin.config.getString("config.format.needmoney")));
                    }
                } else {
                    p.sendMessage(plugin.papi( tag + ChatColor.RED + "Buying Requires Vault and an Economy to work!"));
                }
            } catch (Exception buy) {
                plugin.debug(buy);
                p.sendMessage(plugin.papi(tag + plugin.config.getString("config.format.error") + " " + "commands: " + command));
            }
        } else if (command.split("\\s")[0].equalsIgnoreCase("tokenbuy=")) {
            //if player uses tokenbuy= it will be eg. tokenbuy= <price> <item> <amount of item> <ID>
            try {
                if (plugin.getServer().getPluginManager().isPluginEnabled("TokenManager")) {
                    TokenManager api = (TokenManager) Bukkit.getServer().getPluginManager().getPlugin("TokenManager");
                    assert api != null;
                    int balance = Integer.parseInt(Long.toString(api.getTokens(p).orElse(0)));
                    if (balance >= Double.parseDouble(command.split("\\s")[1])) {
                        api.removeTokens(p, Long.parseLong(command.split("\\s")[1]));
                        //if the message is empty don't send
                        if(!Objects.requireNonNull(plugin.config.getString("config.format.bought")).isEmpty()) {
                            p.sendMessage(plugin.papi( tag + Objects.requireNonNull(plugin.config.getString("config.format.bought")).replaceAll("%cp-args%", command.split("\\s")[1])));
                        }
                        if (p.getInventory().firstEmpty() >= 0) {
                            p.getInventory().addItem(new ItemStack(Objects.requireNonNull(Material.matchMaterial(command.split("\\s")[2])), Integer.parseInt(command.split("\\s")[3])));
                        } else {
                            Objects.requireNonNull(p.getLocation().getWorld()).dropItemNaturally(p.getLocation(), new ItemStack(Objects.requireNonNull(Material.matchMaterial(command.split("\\s")[2])), Integer.parseInt(command.split("\\s")[3])));
                        }
                    } else {
                        p.sendMessage(plugin.papi( tag + plugin.config.getString("config.format.needmoney")));
                    }
                } else {
                    p.sendMessage(plugin.papi( tag + ChatColor.RED + "Buying Requires TokenManager to work!"));
                }
            } catch (Exception buy) {
                plugin.debug(buy);
                p.sendMessage(plugin.papi(tag + plugin.config.getString("config.format.error") + " " + "commands: " + command));
            }
        } else if (command.split("\\s")[0].equalsIgnoreCase("sell=")) {
            //if player uses sell= it will be eg. sell= <cashback> <item> <amount of item> [enchanted:KNOCKBACK:1] [potion:JUMP]
            try {
                if (plugin.econ != null) {
                    boolean sold = false;
                    for (int f = 0; f < p.getInventory().getSize(); f++) {
                        ItemStack itm = p.getInventory().getItem(f);
                        if (itm != null && itm.getType().equals(Material.matchMaterial(command.split("\\s")[2]))) {
                            //determine if the command contains parameters for extensions
                            String potion = "false";
                            for(String argsTemp : command.split("\\s")){
                                if(argsTemp.startsWith("potion:")){
                                    potion = argsTemp.replace("potion:","");
                                }
                            }
                            //check to ensure any extensions are checked
                            try {
                                if (!potion.equals("false")) {
                                    PotionMeta potionMeta = (PotionMeta) itm.getItemMeta();
                                    assert potionMeta != null;
                                    if (!potionMeta.getBasePotionData().getType().name().equalsIgnoreCase(potion)) {
                                        p.sendMessage(plugin.papi( tag + ChatColor.RED + "Your item has the wrong potion effect"));
                                        return;
                                    }
                                }
                            }catch(Exception exc){
                                //skip unless plugin.debug enabled
                                plugin.debug(exc);
                            }
                            if (itm.getAmount() >= new ItemStack(Objects.requireNonNull(Material.matchMaterial(command.split("\\s")[2])), Integer.parseInt(command.split("\\s")[3])).getAmount()) {
                                int amt = itm.getAmount() - new ItemStack(Objects.requireNonNull(Material.matchMaterial(command.split("\\s")[2])), Integer.parseInt(command.split("\\s")[3])).getAmount();
                                itm.setAmount(amt);
                                p.getInventory().setItem(f, amt > 0 ? itm : null);
                                plugin.econ.depositPlayer(p, Double.parseDouble(command.split("\\s")[1]));
                                sold = true;
                                p.updateInventory();
                                break;
                            }
                        }
                    }
                    if (!sold) {
                        p.sendMessage(plugin.papi( tag + plugin.config.getString("config.format.needitems")));
                    } else {
                        //if the message is empty don't send
                        if(!Objects.requireNonNull(plugin.config.getString("config.format.sold")).isEmpty()) {
                            p.sendMessage(plugin.papi( tag + Objects.requireNonNull(plugin.config.getString("config.format.sold")).replaceAll("%cp-args%", command.split("\\s")[1])));
                        }
                    }
                } else {
                    p.sendMessage(plugin.papi( tag + ChatColor.RED + "Selling Requires Vault and an Economy to work!"));
                }
            } catch (Exception sell) {
                plugin.debug(sell);
                p.sendMessage(plugin.papi(tag + plugin.config.getString("config.format.error") + " " + "commands: " + command));
            }
        } else if (command.split("\\s")[0].equalsIgnoreCase("tokensell=")) {
            //if player uses tokensell= it will be eg. tokensell= <cashback> <item> <amount of item> [enchanted:KNOCKBACK:1] [potion:JUMP]
            try {
                if (plugin.getServer().getPluginManager().isPluginEnabled("TokenManager")) {
                    TokenManager api = (TokenManager) Bukkit.getServer().getPluginManager().getPlugin("TokenManager");
                    boolean sold = false;
                    for (int f = 0; f < p.getInventory().getSize(); f++) {
                        ItemStack itm = p.getInventory().getItem(f);
                        if (itm != null && itm.getType().equals(Material.matchMaterial(command.split("\\s")[2]))) {
                            //determine if the command contains parameters for extensions
                            String potion = "false";
                            for(String argsTemp : command.split("\\s")){
                                if(argsTemp.startsWith("potion:")){
                                    potion = argsTemp.replace("potion:","");
                                }
                            }
                            //check to ensure any extensions are checked
                            try {
                                if (!potion.equals("false")) {
                                    PotionMeta potionMeta = (PotionMeta) itm.getItemMeta();
                                    assert potionMeta != null;
                                    if (!potionMeta.getBasePotionData().getType().name().equalsIgnoreCase(potion)) {
                                        p.sendMessage(plugin.papi( tag + ChatColor.RED + "Your item has the wrong potion effect"));
                                        return;
                                    }
                                }
                            }catch(Exception exc){
                                //skip if it cannot do unless plugin.debug is enabled
                                plugin.debug(exc);
                            }
                            if (itm.getAmount() >= new ItemStack(Objects.requireNonNull(Material.matchMaterial(command.split("\\s")[2])), Integer.parseInt(command.split("\\s")[3])).getAmount()) {
                                int amt = itm.getAmount() - new ItemStack(Objects.requireNonNull(Material.matchMaterial(command.split("\\s")[2])), Integer.parseInt(command.split("\\s")[3])).getAmount();
                                itm.setAmount(amt);
                                p.getInventory().setItem(f, amt > 0 ? itm : null);
                                plugin.econ.depositPlayer(p, Double.parseDouble(command.split("\\s")[1]));
                                assert api != null;
                                api.addTokens(p, Long.parseLong(command.split("\\s")[1]));
                                sold = true;
                                p.updateInventory();
                                break;
                            }
                        }
                    }
                    if (!sold) {
                        p.sendMessage(plugin.papi( tag + plugin.config.getString("config.format.needitems")));
                    } else {
                        //if the message is empty don't send
                        if(!Objects.requireNonNull(plugin.config.getString("config.format.sold")).isEmpty()) {
                            p.sendMessage(plugin.papi( tag + Objects.requireNonNull(plugin.config.getString("config.format.sold")).replaceAll("%cp-args%", command.split("\\s")[1])));
                        }
                    }
                } else {
                    p.sendMessage(plugin.papi( tag + ChatColor.RED + "Selling Requires TokenManager to work!"));
                }
            } catch (Exception sell) {
                plugin.debug(sell);
                p.sendMessage(plugin.papi(tag + plugin.config.getString("config.format.error") + " " + "commands: " + command));
            }
        } else if (command.split("\\s")[0].equalsIgnoreCase("msg=")) {
            //if player uses msg= it will send the player a message
            p.sendMessage(plugin.papi(p, command.replace("msg=", "").trim()));
        } else if (command.split("\\s")[0].equalsIgnoreCase("sound=")) {
            //if player uses sound= it will play a sound (sound= [sound])
            try {
                p.playSound(p.getLocation(), Sound.valueOf(command.split("\\s")[1]), 1F, 1F);
            } catch (Exception s) {
                plugin.debug(s);
                p.sendMessage(plugin.papi(tag + plugin.config.getString("config.format.error") + " " + "commands: " + command));
            }
        } else if (command.split("\\s")[0].equalsIgnoreCase("tokenbuycommand=")) {
            //if player uses tokenbuycommand [price] [command]
            try {
                if (plugin.getServer().getPluginManager().isPluginEnabled("TokenManager")) {
                    TokenManager api = (TokenManager) Bukkit.getServer().getPluginManager().getPlugin("TokenManager");
                    assert api != null;
                    int balance = Integer.parseInt(Long.toString(api.getTokens(p).orElse(0)));
                    if (balance >= Double.parseDouble(command.split("\\s")[1])) {
                        api.removeTokens(p, Long.parseLong(command.split("\\s")[1]));
                        //execute command under here
                        String commandp = command;
                        commandp = commandp.replace("tokenbuycommand=", "").trim();
                        String price = commandp.split(" ", 2)[0];
                        commandp = commandp.split(" ", 2)[1];
                        commandTags(p,plugin.papi(p, commandp));
                        //if the message is empty don't send
                        if(!Objects.requireNonNull(plugin.config.getString("config.format.bought")).isEmpty()) {
                            p.sendMessage(plugin.papi( tag + Objects.requireNonNull(plugin.config.getString("config.format.bought")).replaceAll("%cp-args%", price)));
                        }
                    } else {
                        p.sendMessage(plugin.papi( tag + plugin.config.getString("config.format.needmoney")));
                    }
                } else {
                    p.sendMessage(plugin.papi( tag + ChatColor.RED + "Buying Requires Vault and an Economy to work!"));
                }
            } catch (Exception buyc) {
                plugin.debug(buyc);
                p.sendMessage(plugin.papi(tag + plugin.config.getString("config.format.error") + " " + "commands: " + command));
            }
        } else if (command.split("\\s")[0].equalsIgnoreCase("buycommand=")) {
            //if player uses buycommand [price] [command]
            try {
                if (plugin.econ != null) {
                    if (plugin.econ.getBalance(p) >= Double.parseDouble(command.split("\\s")[1])) {
                        plugin.econ.withdrawPlayer(p, Double.parseDouble(command.split("\\s")[1]));
                        //execute command under here
                        String commandp = command;
                        commandp = commandp.replace("buycommand=", "").trim();
                        String price = commandp.split(" ", 2)[0];
                        commandp = commandp.split(" ", 2)[1];
                        commandTags(p,plugin.papi(p, commandp));
                        //if the message is empty don't send
                        if(!Objects.requireNonNull(plugin.config.getString("config.format.bought")).isEmpty()) {
                            p.sendMessage(plugin.papi( tag + Objects.requireNonNull(plugin.config.getString("config.format.bought")).replaceAll("%cp-args%", price)));
                        }
                    } else {
                        p.sendMessage(plugin.papi( tag + plugin.config.getString("config.format.needmoney")));
                    }
                } else {
                    p.sendMessage(plugin.papi( tag + ChatColor.RED + "Buying Requires Vault and an Economy to work!"));
                }
            } catch (Exception buyc) {
                plugin.debug(buyc);
                p.sendMessage(plugin.papi(tag + plugin.config.getString("config.format.error") + " " + "commands: " + command));
            }
        } else if (command.split("\\s")[0].equalsIgnoreCase("teleport=")) {
            //if player uses teleport= x y z (optional other player)
            if (command.split("\\s").length == 6) {
                float x, y, z, yaw, pitch; //pitch is the heads Y axis and yaw is the X axis
                x = Float.parseFloat(plugin.papi(p, command.split("\\s")[1]));
                y = Float.parseFloat(plugin.papi(p, command.split("\\s")[2]));
                z = Float.parseFloat(plugin.papi(p, command.split("\\s")[3]));
                yaw = Float.parseFloat(plugin.papi(p, command.split("\\s")[4]));
                pitch = Float.parseFloat(plugin.papi(p, command.split("\\s")[5]));
                p.teleport(new Location(p.getWorld(), x, y, z, yaw, pitch));
            } else if (command.split("\\s").length <= 4) {
                float x, y, z;
                x = Float.parseFloat(plugin.papi(p, command.split("\\s")[1]));
                y = Float.parseFloat(plugin.papi(p, command.split("\\s")[2]));
                z = Float.parseFloat(plugin.papi(p, command.split("\\s")[3]));
                p.teleport(new Location(p.getWorld(), x, y, z));
            } else {
                try {
                    Player otherplayer = Bukkit.getPlayer(plugin.papi(p, command.split("\\s")[4]));
                    float x, y, z;
                    x = Float.parseFloat(plugin.papi(p, command.split("\\s")[1]));
                    y = Float.parseFloat(plugin.papi(p, command.split("\\s")[2]));
                    z = Float.parseFloat(plugin.papi(p, command.split("\\s")[3]));
                    assert otherplayer != null;
                    otherplayer.teleport(new Location(otherplayer.getWorld(), x, y, z));
                } catch (Exception tpe) {
                    p.sendMessage(tag + plugin.config.getString("config.format.notitem"));
                }
            }
        } else if (command.split("\\s")[0].equalsIgnoreCase("stopsound=")) {
            //if player uses stopsound= [sound]
            try {
                p.stopSound(Sound.valueOf(command.split("\\s")[1]));
            } catch (Exception ss) {
                plugin.debug(ss);
                p.sendMessage(plugin.papi(tag + plugin.config.getString("config.format.error") + " " + "commands: " + command));
            }
        } else if (command.split("\\s")[0].equalsIgnoreCase("sudo=")) {
            //if player uses sudo= [command]
            p.chat(plugin.papi(p, "/" + command.replaceAll("sudo=", "").trim()));
        } else {
            Bukkit.dispatchCommand(p, plugin.papi(p, command));
        }
    }

    public int commandPayWall(Player p, String command) { //return 0 means no funds, 1 is they passed and 2 means paywall is not this command
        String tag = plugin.config.getString("config.format.tag") + " ";
        if (command.split("\\s")[0].equalsIgnoreCase("paywall=")) {
            //if player uses paywall= [price]
            try {
                if (plugin.econ != null) {
                    if (plugin.econ.getBalance(p) >= Double.parseDouble(command.split("\\s")[1])) {
                        plugin.econ.withdrawPlayer(p, Double.parseDouble(command.split("\\s")[1]));
                        //if the message is empty don't send
                        if(!plugin.config.getString("config.format.bought").isEmpty()) {
                            p.sendMessage(plugin.papi( tag + Objects.requireNonNull(plugin.config.getString("config.format.bought")).replaceAll("%cp-args%", command.split("\\s")[1])));
                        }
                        return 1;
                    } else {
                        p.sendMessage(plugin.papi( tag + plugin.config.getString("config.format.needmoney")));
                        return 0;
                    }
                } else {
                    p.sendMessage(plugin.papi( tag + ChatColor.RED + "Paying Requires Vault and an Economy to work!"));
                    return 0;
                }
            } catch (Exception buyc) {
                plugin.debug(buyc);
                p.sendMessage(plugin.papi(p, tag + plugin.config.getString("config.format.error") + " " + "commands: " + command));
                return 0;
            }
        } else if (command.split("\\s")[0].equalsIgnoreCase("tokenpaywall=")) {
            //if player uses tokenpaywall= [price]
            try {
                if (plugin.getServer().getPluginManager().isPluginEnabled("TokenManager")) {
                    TokenManager api = (TokenManager) Bukkit.getServer().getPluginManager().getPlugin("TokenManager");
                    assert api != null;
                    int balance = Integer.parseInt(Long.toString(api.getTokens(p).orElse(0)));
                    if (balance >= Double.parseDouble(command.split("\\s")[1])) {
                        api.removeTokens(p, Long.parseLong(command.split("\\s")[1]));
                        //if the message is empty don't send
                        if(!Objects.requireNonNull(plugin.config.getString("config.format.bought")).isEmpty()) {
                            p.sendMessage(plugin.papi( tag + Objects.requireNonNull(plugin.config.getString("config.format.bought")).replaceAll("%cp-args%", command.split("\\s")[1])));
                        }
                        return 1;
                    } else {
                        p.sendMessage(plugin.papi( tag + plugin.config.getString("config.format.needmoney")));
                        return 0;
                    }
                } else {
                    p.sendMessage(plugin.papi( tag + ChatColor.RED + "Paying TokenManager to work!"));
                    return 0;
                }
            } catch (Exception buyc) {
                plugin.debug(buyc);
                p.sendMessage(plugin.papi(p, tag + plugin.config.getString("config.format.error") + " " + "commands: " + command));
                return 0;
            }
        }else if (command.split("\\s")[0].equalsIgnoreCase("item-paywall=")) {
            //if player uses item-paywall= [Material] [Amount]
            try {
                ItemStack sellItem = new ItemStack(Objects.requireNonNull(Material.matchMaterial(command.split("\\s")[1])),Integer.parseInt(command.split("\\s")[2]));
                int sellItemAmount = sellItem.getAmount();
                sellItem.setAmount(1);
                int removedItem = 0;
                for(ItemStack content : p.getInventory().getContents()){
                    int contentAmount;
                    try {
                        contentAmount = content.getAmount();
                    }catch(NullPointerException skip){
                        //item is air
                        continue;
                    }
                    content.setAmount(1);
                    if(content.isSimilar(sellItem)){
                        if(sellItemAmount <= contentAmount){
                            content.setAmount(contentAmount-sellItemAmount);
                            p.updateInventory();
                            removedItem = 1;
                            break;
                        }
                    }
                    content.setAmount(contentAmount);
                }
                if(removedItem == 0){
                    p.sendMessage(plugin.papi( tag + plugin.config.getString("config.format.needmoney")));
                }else{
                    if(!Objects.requireNonNull(plugin.config.getString("config.format.sold")).isEmpty()) {
                        p.sendMessage(plugin.papi( tag + plugin.config.getString("config.format.sold")));
                    }
                }
                return removedItem;
            } catch (Exception buyc) {
                plugin.debug(buyc);
                p.sendMessage(plugin.papi(p, tag + plugin.config.getString("config.format.error") + " " + "commands: " + command));
                return 0;
            }
        }else if (command.split("\\s")[0].equalsIgnoreCase("xp-paywall=")) {
            //if player uses xp-paywall= [price]
            try {
                int balance = p.getLevel();
                if (balance >= Integer.parseInt(command.split("\\s")[1])) {
                    p.setLevel(p.getLevel() - Integer.parseInt(command.split("\\s")[1]));
                    //if the message is empty don't send
                    if(!Objects.requireNonNull(plugin.config.getString("config.format.bought")).isEmpty()) {
                        p.sendMessage(plugin.papi( tag + Objects.requireNonNull(plugin.config.getString("config.format.bought")).replaceAll("%cp-args%", command.split("\\s")[1])));
                    }
                    return 1;
                } else {
                    p.sendMessage(plugin.papi( tag + plugin.config.getString("config.format.needmoney")));
                    return 0;
                }
            } catch (Exception buyc) {
                plugin.debug(buyc);
                p.sendMessage(plugin.papi(p, tag + plugin.config.getString("config.format.error") + " " + "commands: " + command));
                return 0;
            }
        } else {
            return 2;
        }
    }
}
