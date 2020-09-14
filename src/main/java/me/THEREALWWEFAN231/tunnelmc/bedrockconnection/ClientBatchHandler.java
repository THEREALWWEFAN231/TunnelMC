package me.THEREALWWEFAN231.tunnelmc.bedrockconnection;

import java.util.Collection;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.nukkitx.protocol.bedrock.BedrockPacket;
import com.nukkitx.protocol.bedrock.BedrockSession;
import com.nukkitx.protocol.bedrock.handler.BatchHandler;

import io.netty.buffer.ByteBuf;
import me.THEREALWWEFAN231.tunnelmc.TunnelMC;

public class ClientBatchHandler implements BatchHandler {

	private Logger logger = LogManager.getLogger(ClientBatchHandler.class);

	public void handle(BedrockSession session, ByteBuf compressed, Collection<BedrockPacket> packets) {

		for (BedrockPacket packet : packets) {
			if (session.isLogging()) {
				//so yeah.... the default logger, in nukkitx is kind of lame, and in our case trace isn't enabled so we will just do this for now
				this.logger.info("Inbound {}: {}", session.getAddress(), packet.toString().substring(0, Math.min(packet.toString().length(), 200)));
			}
			
			TunnelMC.instance.packetTranslatorManager.translatePacket(packet);
			
			//packet.handle(session.getPacketHandler());
		}

	}

}
