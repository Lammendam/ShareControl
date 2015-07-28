/*******************************************************************************
 * Copyright (C) 2015 H1KaRo (h1karo)
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 ******************************************************************************/


package com.net.h1karo.sharecontrol.listeners.blocks;

import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockFromToEvent;
import com.net.h1karo.sharecontrol.ShareControl;
import com.net.h1karo.sharecontrol.listeners.BasicHandlers;

public class BlockFromToListener implements Listener
{
	@SuppressWarnings("unused")
	private final ShareControl main;
	
	public BlockFromToListener(ShareControl h)
	{
		this.main = h;
	}
	
	@EventHandler
	public void WaterBreakBlock(BlockFromToEvent e) {
		String[] WBD = new String[29];
		BasicHandlers.WaterDropBlocks(WBD);
		Block b = e.getToBlock();
		for(int i = 0; i < WBD.length; i++)
		if(b.getType() == Material.getMaterial(WBD[i]) && BasicHandlers.InBase(b))
		{
			e.setCancelled(true);
			b.setType(Material.AIR);
			BasicHandlers.OnBase(b);
			return;
		}
	}
}
