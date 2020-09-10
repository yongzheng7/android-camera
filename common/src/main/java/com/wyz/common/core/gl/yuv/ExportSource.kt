package com.wyz.common.core.gl.yuv


class ExportSource {

    companion object{
        private val HEAD = """precision highp float;
                            precision highp int;
                            
                            varying vec2 vTextureCo;
                            uniform sampler2D uTexture;
                            
                            uniform float uWidth;
                            uniform float uHeight;
                            
                            float cY(float x,float y){
                                vec4 c=texture2D(uTexture,vec2(x,y));
                                return c.r*0.257+c.g*0.504+c.b*0.098+0.0625;
                            }
                            
                            vec4 cC(float x,float y,float dx,float dy){
                                vec4 c0=texture2D(uTexture,vec2(x,y));
                                vec4 c1=texture2D(uTexture,vec2(x+dx,y));
                                vec4 c2=texture2D(uTexture,vec2(x,y+dy));
                                vec4 c3=texture2D(uTexture,vec2(x+dx,y+dy));
                                return (c0+c1+c2+c3)/4.;
                            }
                            
                            float cU(float x,float y,float dx,float dy){
                                vec4 c=cC(x,y,dx,dy);
                                return -0.148*c.r - 0.291*c.g + 0.439*c.b+0.5000;
                            }
                            
                            float cV(float x,float y,float dx,float dy){
                                vec4 c=cC(x,y,dx,dy);
                                return 0.439*c.r - 0.368*c.g - 0.071*c.b+0.5000;
                            }
                            
                            vec2 cPos(float t,float shiftx,float gy){
                                vec2 pos=vec2(floor(uWidth*vTextureCo.x),floor(uHeight*gy));
                                return vec2(mod(pos.x*shiftx,uWidth),(pos.y*shiftx+floor(pos.x*shiftx/uWidth))*t);
                            }
                            
                            vec4 calculateY(){
                                vec2 pos=cPos(1.,4.,vTextureCo.y);
                                vec4 oColor=vec4(0);
                                float textureYPos=pos.y/uHeight;
                                oColor[0]=cY(pos.x/uWidth,textureYPos);
                                oColor[1]=cY((pos.x+1.)/uWidth,textureYPos);
                                oColor[2]=cY((pos.x+2.)/uWidth,textureYPos);
                                oColor[3]=cY((pos.x+3.)/uWidth,textureYPos);
                                return oColor;
                            }
                            vec4 calculateU(float gy,float dx,float dy){
                                vec2 pos=cPos(2.,8.,vTextureCo.y-gy);
                                vec4 oColor=vec4(0);
                                float textureYPos=pos.y/uHeight;
                                oColor[0]= cU(pos.x/uWidth,textureYPos,dx,dy);
                                oColor[1]= cU((pos.x+2.)/uWidth,textureYPos,dx,dy);
                                oColor[2]= cU((pos.x+4.)/uWidth,textureYPos,dx,dy);
                                oColor[3]= cU((pos.x+6.)/uWidth,textureYPos,dx,dy);
                                return oColor;
                            }
                            vec4 calculateV(float gy,float dx,float dy){
                                vec2 pos=cPos(2.,8.,vTextureCo.y-gy);
                                vec4 oColor=vec4(0);
                                float textureYPos=pos.y/uHeight;
                                oColor[0]=cV(pos.x/uWidth,textureYPos,dx,dy);
                                oColor[1]=cV((pos.x+2.)/uWidth,textureYPos,dx,dy);
                                oColor[2]=cV((pos.x+4.)/uWidth,textureYPos,dx,dy);
                                oColor[3]=cV((pos.x+6.)/uWidth,textureYPos,dx,dy);
                                return oColor;
                            }
                            vec4 calculateUV(float dx,float dy){
                                vec2 pos=cPos(2.,4.,vTextureCo.y-0.2500);
                                vec4 oColor=vec4(0);
                                float textureYPos=pos.y/uHeight;
                                oColor[0]= cU(pos.x/uWidth,textureYPos,dx,dy);
                                oColor[1]= cV(pos.x/uWidth,textureYPos,dx,dy);
                                oColor[2]= cU((pos.x+2.)/uWidth,textureYPos,dx,dy);
                                oColor[3]= cV((pos.x+2.)/uWidth,textureYPos,dx,dy);
                                return oColor;
                            }
                            vec4 calculateVU(float dx,float dy){
                                vec2 pos=cPos(2.,4.,vTextureCo.y-0.2500);
                                vec4 oColor=vec4(0);
                                float textureYPos=pos.y/uHeight;
                                oColor[0]= cV(pos.x/uWidth,textureYPos,dx,dy);
                                oColor[1]= cU(pos.x/uWidth,textureYPos,dx,dy);
                                oColor[2]= cV((pos.x+2.)/uWidth,textureYPos,dx,dy);
                                oColor[3]= cU((pos.x+2.)/uWidth,textureYPos,dx,dy);
                                return oColor;
                            }
                            """

        fun getFrag(type: Int): String? {
            val sb = StringBuilder()
            sb.append(HEAD)
            when (type) {
                YuvOutputShader.EXPORT_TYPE_I420 -> sb.append("""void main() {
                                                                    if(vTextureCo.y<0.2500){
                                                                        gl_FragColor=calculateY();
                                                                    }else if(vTextureCo.y<0.3125){
                                                                        gl_FragColor=calculateU(0.2500,1./uWidth,1./uHeight);
                                                                    }else if(vTextureCo.y<0.3750){
                                                                        gl_FragColor=calculateV(0.3125,1./uWidth,1./uHeight);
                                                                    }else{
                                                                        gl_FragColor=vec4(0,0,0,0);
                                                                    }
                                                                }""")
                YuvOutputShader.EXPORT_TYPE_YV12 -> sb.append("""void main() {
                                                                    if(vTextureCo.y<0.2500){
                                                                        gl_FragColor=calculateY();
                                                                    }else if(vTextureCo.y<0.3125){
                                                                        gl_FragColor=calculateV(0.2500,1./uWidth,1./uHeight);
                                                                    }else if(vTextureCo.y<0.3750){
                                                                        gl_FragColor=calculateU(0.3125,1./uWidth,1./uHeight);
                                                                    }else{
                                                                        gl_FragColor=vec4(0,0,0,0);
                                                                    }
                                                                }""")
                YuvOutputShader.EXPORT_TYPE_NV12 -> sb.append("""void main() {
                                                                    if(vTextureCo.y<0.2500){
                                                                        gl_FragColor=calculateY();
                                                                    }else if(vTextureCo.y<0.3750){
                                                                        gl_FragColor=calculateUV(1./uWidth,1./uHeight);
                                                                    }else{
                                                                        gl_FragColor=vec4(0,0,0,0);
                                                                    }
                                                                }""")
                YuvOutputShader.EXPORT_TYPE_NV21 -> sb.append("""void main() {
                                                        if(vTextureCo.y<0.2500){
                                                            gl_FragColor=calculateY();
                                                        }else if(vTextureCo.y<0.3750){
                                                            gl_FragColor=calculateVU(1./uWidth,1./uHeight);
                                                        }else{
                                                            gl_FragColor=vec4(0,0,0,0);
                                                        }
                                                    }""")
                else -> sb.append("""void main() {
                                        if(vTextureCo.y<0.2500){
                                            gl_FragColor=calculateY();
                                        }else if(vTextureCo.y<0.3750){
                                            gl_FragColor=calculateVU(1./uWidth,1./uHeight);
                                        }else{
                                            gl_FragColor=vec4(0,0,0,0);
                                        }
                                    }""")
            }
            return sb.toString()
        }
    }
}