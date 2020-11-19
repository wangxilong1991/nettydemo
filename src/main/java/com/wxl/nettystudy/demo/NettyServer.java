package com.wxl.nettystudy.demo;

import io.netty.bootstrap.ServerBootstrap;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.*;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;

public class NettyServer {

    private int port;

    public NettyServer(int port) throws Exception {
        this.port = port;
        bind();
    }

    private void bind() throws Exception {
        EventLoopGroup boss = new NioEventLoopGroup(1);
        EventLoopGroup works = new NioEventLoopGroup(2);

        try {
            ServerBootstrap bootstrap = new ServerBootstrap();
            bootstrap.group(boss, works)
                    .channel(NioServerSocketChannel.class)
                    .option(ChannelOption.SO_BACKLOG,1024)//连接数
//                    .option(ChannelOption.TCP_NODELAY,true)//不延时，消息即时发出
                    .childOption(ChannelOption.SO_KEEPALIVE,true)//长链接
                    .childHandler(new ChannelInitializer<SocketChannel>() {
                        @Override
                        protected void initChannel(SocketChannel ch) throws Exception {
                            ChannelPipeline pipeline = ch.pipeline();
                            pipeline.addLast(new NettyServerHandler());//添加NettyServerHandler，用来处理Server端接收和处理消息的逻辑
                        }
                    });
            ChannelFuture channelFuture = bootstrap.bind(port).sync();
            if(channelFuture.isSuccess()){
                System.err.println("启动netty服务成功；端口号为： "+port);
            }
            //关闭链接
            channelFuture.channel().closeFuture().sync();

        }catch (Exception e){
            e.printStackTrace();
            throw e;
        }finally {
            works.shutdownGracefully();
            boss.shutdownGracefully();
        }

    }

    public static void main(String[] args) throws Exception {
        new NettyServer(10086);
    }
}

class NettyServerHandler extends ChannelInboundHandlerAdapter{
    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
        ByteBuf byteBuf = null;
        try {
            byteBuf = (ByteBuf)msg;

            byte[] bytes = new byte[byteBuf.readableBytes()];
            byteBuf.getBytes(byteBuf.readerIndex(),bytes);

            System.out.print("服务端接收到消息 -> ");
            String str = new String(bytes);
            System.out.println(str);

            String sendMsg = "服务端回复给客户端:"+str;

            ByteBuf senBuf = Unpooled.buffer();
            senBuf.writeBytes(sendMsg.getBytes());
            ctx.writeAndFlush(senBuf);
        } catch (Exception e) {
            e.printStackTrace();
            throw e;
        }
    }

    @Override
    public void channelActive(ChannelHandlerContext ctx) throws Exception {
        super.channelActive(ctx);
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
        cause.printStackTrace();
        super.exceptionCaught(ctx,cause);
    }
}