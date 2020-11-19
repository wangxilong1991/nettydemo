package com.wxl.nettystudy.demo;

import io.netty.bootstrap.Bootstrap;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.*;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;

public class NettyClient {

    private String serverHost;
    private int intPort;

    public NettyClient(String host,int port) throws InterruptedException {
        this.serverHost = host;
        this.intPort = port;
        start();
    }

    public void start() throws InterruptedException {
        EventLoopGroup eventLoopGroup = new NioEventLoopGroup(1);

        try {
            Bootstrap bootstrap = new Bootstrap();
            bootstrap.group(eventLoopGroup)
                    .channel(NioSocketChannel.class)
                    .option(ChannelOption.SO_KEEPALIVE,false)
                    .remoteAddress(serverHost,intPort)
                    .handler(new ChannelInitializer<SocketChannel>() {
                        @Override
                        protected void initChannel(SocketChannel ch) throws Exception {
                            ChannelPipeline pipeline = ch.pipeline();
                            pipeline.addLast(new NettyClientHandler());
                        }
                    });

            ChannelFuture channelFuture = bootstrap.connect(serverHost, intPort).sync();
            if(channelFuture.isSuccess()){
                System.out.println("链接服务端成功！");
            }
            channelFuture.channel().closeFuture().sync();
        } catch (Exception e) {
            e.printStackTrace();
            throw e;
        } finally {
            eventLoopGroup.shutdownGracefully();
        }
    }

    public static void main(String[] args) throws InterruptedException {
        NettyClient nettyClient = new NettyClient("localhost",10086);
    }
}

class NettyClientHandler extends ChannelInboundHandlerAdapter{
    private ByteBuf firstMessage;
    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
        ByteBuf byteBuf = null;
        try{
            byteBuf = (ByteBuf)msg;

            byte[] bys = new byte[byteBuf.readableBytes()];
            byteBuf.getBytes(byteBuf.readerIndex(),bys);

            System.out.println(new String(bys));

        }catch (Exception e){
            e.printStackTrace();
            throw e;
        }
    }

    @Override
    public void channelActive(ChannelHandlerContext ctx) throws Exception {
        byte[] first = ("你好服务器"+Thread.currentThread().getName()).getBytes();
        firstMessage = Unpooled.buffer();
        firstMessage.writeBytes(first);
        ctx.writeAndFlush(firstMessage);
        System.err.println("客户端发送消息:你好，服务器");
    }
}