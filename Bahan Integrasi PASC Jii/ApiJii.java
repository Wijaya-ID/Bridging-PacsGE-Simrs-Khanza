/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package bridging;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import fungsi.koneksiDB;
import fungsi.sekuel;
import java.security.KeyManagementException;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.Arrays;
import javax.net.ssl.SSLContext;
import javax.net.ssl.X509TrustManager;
import javax.swing.JOptionPane;
import org.apache.http.conn.scheme.Scheme;
import org.apache.http.conn.ssl.SSLSocketFactory;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.http.client.HttpComponentsClientHttpRequestFactory;
import org.springframework.web.client.RestTemplate;

/**
 *
 * @author khanzasoft
 */
public class ApiJii {
    private int a,j,i,result=0;
    private Connection koneksi=koneksiDB.condb();
    private PreparedStatement ps,ps2;
    private sekuel Sequel=new sekuel();
    private ResultSet rs,rs2,rs3;
    private String URL="",URLHASIL="",UN="",PW="",requestJson="",stringbalik="",tindakan="",order="",modal="",id="";
    private String[] nm_tindakan,no_order,mdl;
    private int loop, jumlah,idd;
    private HttpHeaders headers;
    private HttpEntity requestEntity;
    private JsonNode root;
    private JsonNode response;
    private ObjectMapper mapper = new ObjectMapper();
    
    public ApiJii(){
        super();
        try {
            URL = koneksiDB.URLJII();
            URLHASIL = koneksiDB.URLJII();
            UN = koneksiDB.UNJII();
            PW = koneksiDB.PWJII();
            id = koneksiDB.IDJII();
            idd = Integer.parseInt(id);
        } catch (Exception e) {
            System.out.println("Notif : "+e);
        }
    }
    
    public void kirimRalan(String nopermintaan) {
        
        try {
             ps=koneksi.prepareStatement(
                    "select permintaan_radiologi.noorder,permintaan_radiologi.no_rawat,reg_periksa.no_rkm_medis,pasien.nm_pasien,permintaan_radiologi.tgl_permintaan,"+
                    "if(permintaan_radiologi.jam_permintaan='00:00:00','',permintaan_radiologi.jam_permintaan) as jam_permintaan,pasien.tgl_lahir,pasien.jk,pasien.alamat,"+
                    "if(permintaan_radiologi.tgl_sampel='0000-00-00','',permintaan_radiologi.tgl_sampel) as tgl_sampel,if(permintaan_radiologi.jam_sampel='00:00:00','',permintaan_radiologi.jam_sampel) as jam_sampel,"+
                    "if(permintaan_radiologi.tgl_hasil='0000-00-00','',permintaan_radiologi.tgl_hasil) as tgl_hasil,if(permintaan_radiologi.jam_hasil='00:00:00','',permintaan_radiologi.jam_hasil) as jam_hasil,"+
                    "permintaan_radiologi.dokter_perujuk,dokter.nm_dokter,poliklinik.nm_poli,pasien.no_tlp,penjab.png_jawab,permintaan_pemeriksaan_radiologi.kd_jenis_prw,jns_perawatan_radiologi.nm_perawatan, "+
                    "permintaan_radiologi.diagnosa_klinis,permintaan_radiologi.informasi_tambahan,kabupaten.nm_kab from permintaan_radiologi "+
                    "inner join reg_periksa on permintaan_radiologi.no_rawat=reg_periksa.no_rawat "+
                    "inner join pasien on reg_periksa.no_rkm_medis=pasien.no_rkm_medis "+
                    "inner join kabupaten ON pasien.kd_kab = kabupaten.kd_kab "+
                    "inner join dokter on permintaan_radiologi.dokter_perujuk=dokter.kd_dokter "+
                    "inner join poliklinik on reg_periksa.kd_poli=poliklinik.kd_poli "+
                    "inner join penjab on reg_periksa.kd_pj=penjab.kd_pj "+
                    "inner join permintaan_pemeriksaan_radiologi on permintaan_pemeriksaan_radiologi.noorder=permintaan_radiologi.noorder "+
                    "inner join jns_perawatan_radiologi on permintaan_pemeriksaan_radiologi.kd_jenis_prw=jns_perawatan_radiologi.kd_jenis_prw "+
                    "where permintaan_radiologi.noorder=? "+
                    "group by permintaan_pemeriksaan_radiologi.kd_jenis_prw");
             try {     
                ps.setString(1,nopermintaan);
                rs3=ps.executeQuery();
                 jumlah = 0;
                 while(rs3.next()){
                     jumlah = jumlah + 1;
                } 
                //System.out.println(jumlah);
                ps.setString(1,nopermintaan);
                rs2=ps.executeQuery();
                nm_tindakan = new String[jumlah];
                no_order = new String[jumlah];
                mdl = new String[jumlah];
                 loop = 0;
                 while(rs2.next()){
                     no_order[loop]= rs2.getString("noorder")+rs2.getString("kd_jenis_prw").substring(idd);
                     nm_tindakan[loop]= rs2.getString("nm_perawatan");
                     mdl[loop]= rs2.getString("kd_jenis_prw").substring(0,2);
                     loop = loop + 1;
                }
                // tindakan = hasil[0]+hasil[1]+hasil[3];
                order = Arrays.toString(no_order);
                tindakan = Arrays.toString(nm_tindakan);
                modal = Arrays.toString(mdl);
//                System.out.println(order);
//                System.out.println(tindakan);
//                System.out.println(modal);
                ps.setString(1,nopermintaan);
                rs=ps.executeQuery();
                while(rs.next()){
                    headers = new HttpHeaders();
                    headers.setContentType(MediaType.APPLICATION_JSON);
                    headers.add("Content-Type","application/json;charset=UTF-8");
                    requestJson="{" +
                                    "\"no_rkm_medis\": \""+rs.getString("no_rkm_medis")+"\"," +
                                    "\"nm_pasien\": \""+rs.getString("nm_pasien")+"\"," +
                                    "\"tgl_lahir\": \""+rs.getString("tgl_lahir")+"\"," +
                                    "\"sex\": \""+rs.getString("jk").replaceAll("L","M").replaceAll("P","F")+"\"," +
                                    "\"alamat\": \""+rs.getString("alamat")+"\"," +
                                    "\"kota\": \""+rs.getString("nm_kab")+"\"," +
                                    "\"nm_dokter\": \""+rs.getString("nm_dokter")+"\"," +
                                    "\"nm_poli\": \""+rs.getString("nm_poli")+"\"," +
                                    "\"informasi_tambahan\": \""+(rs.getString("informasi_tambahan").toLowerCase().contains("cito")?"CITO":"")+"\"," +
                                    "\"tgl_periksa\": \""+rs.getString("tgl_permintaan")+" "+rs.getString("jam_permintaan")+"\"," +
                                    "\"acc_number\": "+order.replaceAll(",","\",\"").replaceAll("\\[","\\[\"").replaceAll("\\]","\"\\]").replaceAll(" ","")+"," +
                                    "\"nm_perawatan\": "+tindakan.replaceAll(",","\",\"").replaceAll("\\[","\\[\"").replaceAll("\\]","\"\\]").replaceAll(" ","")+"," +
                                    "\"modality\": "+modal.replaceAll(",","\",\"").replaceAll("\\[","\\[\"").replaceAll("\\]","\"\\]").replaceAll(" ","") +
                                "}"; 
                    System.out.println("JSON : "+requestJson);
                    System.out.println("URL : "+URL+"/order/add");
                    requestEntity = new HttpEntity(requestJson,headers);	    
                    stringbalik=getRest().exchange(URL+"/order/add", HttpMethod.POST, requestEntity, String.class).getBody();
                    JOptionPane.showMessageDialog(null,stringbalik);
                }
             } catch (Exception e) {
                 System.out.println("Notif : "+e);
                 if(e.toString().contains("UnknownHostException")||e.toString().contains("404")){
                    JOptionPane.showMessageDialog(null,"Koneksi ke server Jii terputus...!");
                 }
             } finally{
                 if(rs!=null){
                     rs.close();
                 }
                 if(ps!=null){
                     ps.close();
                 }
             }
        } catch (Exception ex) {
            System.out.println("Notifikasi : "+ex);
            if(ex.toString().contains("UnknownHostException")||ex.toString().contains("404")){
                JOptionPane.showMessageDialog(null,"Koneksi ke server Jii terputus...!");
            }
        }
    }
    
    public void ambilRalan(String nopermintaan) {
        try {
             ps=koneksi.prepareStatement(
                    "select permintaan_radiologi.noorder,permintaan_radiologi.no_rawat,reg_periksa.no_rkm_medis,pasien.nm_pasien,permintaan_radiologi.tgl_permintaan,"+
                    "if(permintaan_radiologi.jam_permintaan='00:00:00','',permintaan_radiologi.jam_permintaan) as jam_permintaan,pasien.tgl_lahir,pasien.jk,pasien.alamat,"+
                    "if(permintaan_radiologi.tgl_sampel='0000-00-00','',permintaan_radiologi.tgl_sampel) as tgl_sampel,if(permintaan_radiologi.jam_sampel='00:00:00','',permintaan_radiologi.jam_sampel) as jam_sampel,"+
                    "if(permintaan_radiologi.tgl_hasil='0000-00-00','',permintaan_radiologi.tgl_hasil) as tgl_hasil,if(permintaan_radiologi.jam_hasil='00:00:00','',permintaan_radiologi.jam_hasil) as jam_hasil,"+
                    "permintaan_radiologi.dokter_perujuk,dokter.nm_dokter,poliklinik.nm_poli,pasien.no_tlp,penjab.png_jawab,permintaan_pemeriksaan_radiologi.kd_jenis_prw,jns_perawatan_radiologi.nm_perawatan, "+
                    "permintaan_radiologi.diagnosa_klinis,permintaan_radiologi.informasi_tambahan,kabupaten.nm_kab from permintaan_radiologi "+
                    "inner join reg_periksa on permintaan_radiologi.no_rawat=reg_periksa.no_rawat "+
                    "inner join pasien on reg_periksa.no_rkm_medis=pasien.no_rkm_medis "+
                    "inner join kabupaten ON pasien.kd_kab = kabupaten.kd_kab "+
                    "inner join dokter on permintaan_radiologi.dokter_perujuk=dokter.kd_dokter "+
                    "inner join poliklinik on reg_periksa.kd_poli=poliklinik.kd_poli "+
                    "inner join penjab on reg_periksa.kd_pj=penjab.kd_pj "+
                    "inner join permintaan_pemeriksaan_radiologi on permintaan_pemeriksaan_radiologi.noorder=permintaan_radiologi.noorder "+
                    "inner join jns_perawatan_radiologi on permintaan_pemeriksaan_radiologi.kd_jenis_prw=jns_perawatan_radiologi.kd_jenis_prw "+
                    "where permintaan_radiologi.noorder=? "+
                    "group by permintaan_pemeriksaan_radiologi.kd_jenis_prw");
             try {
                ps.setString(1,nopermintaan);
                rs3=ps.executeQuery();
                 jumlah = 0;
                 while(rs3.next()){
                     jumlah = jumlah + 1;
                } 
                //System.out.println(jumlah);
                ps.setString(1,nopermintaan);
                rs2=ps.executeQuery();
                nm_tindakan = new String[jumlah];
                no_order = new String[jumlah];
                mdl = new String[jumlah];
                 loop = 0;
                 while(rs2.next()){
                     no_order[loop]= +loop+"\":\""+rs2.getString("noorder")+rs2.getString("kd_jenis_prw").substring(idd);
                     nm_tindakan[loop]= +loop+"\":\""+rs2.getString("nm_perawatan");
                     mdl[loop]= +loop+"\":\""+rs2.getString("kd_jenis_prw").substring(0,2);
                     loop = loop + 1;
                }
                // tindakan = hasil[0]+hasil[1]+hasil[3];
                order = Arrays.toString(no_order);
                tindakan = Arrays.toString(nm_tindakan);
                modal = Arrays.toString(mdl);
                ps.setString(1,nopermintaan);
                rs=ps.executeQuery();
                while(rs.next()){
                    headers = new HttpHeaders();
                    headers.setContentType(MediaType.APPLICATION_JSON);
                    headers.add("Content-Type","application/json;charset=UTF-8");
                    requestJson="{" +      
                                    "\"acc_number\":{"+order.replaceAll(",","\",\"").replaceAll("\\[","\"").replaceAll("\\]","\"\\},").replaceAll(" ","")+
                                    "\"no_rkm_medis\": \""+rs.getString("no_rkm_medis")+"\"," +
                                    "\"nm_pasien\": \""+rs.getString("nm_pasien")+"\"," +
                                    "\"tgl_lahir\": \""+rs.getString("tgl_lahir")+"\"," +
                                    "\"sex\": \""+rs.getString("jk").replaceAll("L","M").replaceAll("P","F")+"\"," +
                                    "\"alamat\": \""+rs.getString("alamat")+"\"," +
                                    "\"kota\": \""+rs.getString("nm_kab")+"\"," +
                                    "\"nama_dokter_baca\": \""+Sequel.cariIsi("select dokter.nm_dokter from periksa_radiologi inner join dokter on periksa_radiologi.kd_dokter = dokter.kd_dokter where periksa_radiologi.no_rawat=? and periksa_radiologi.tgl_periksa=\""+rs.getString("tgl_hasil")+"\"and periksa_radiologi.jam=\""+rs.getString("jam_hasil")+"\"",rs.getString("no_rawat"))+"\"," +
                                    "\"nm_perawatan\":{"+tindakan.replaceAll(",","\",\"").replaceAll("\\[","\"").replaceAll("\\]","\"\\},").replaceAll(" ","") +
                                    "\"nm_poli\": \""+rs.getString("nm_poli")+"\"," +
                                    "\"modality\":{"+modal.replaceAll(",","\",\"").replaceAll("\\[","\"").replaceAll("\\]","\"\\},").replaceAll(" ","") +
                                    "\"tgl_periksa\": \""+rs.getString("tgl_permintaan")+" "+rs.getString("jam_permintaan")+"\"," +
                                    "\"keterangan\": \""+Sequel.cariIsi("select hasil_radiologi.hasil from hasil_radiologi where hasil_radiologi.no_rawat=? and hasil_radiologi.tgl_periksa=\""+rs.getString("tgl_hasil")+"\"and hasil_radiologi.jam=\""+rs.getString("jam_hasil")+"\"",rs.getString("no_rawat"))+"\"" +         
                                "}"; 
                    System.out.println("JSON : "+requestJson);
                    System.out.println("URL : "+URL+"/expertise/add");
                    requestEntity = new HttpEntity(requestJson,headers);	    
                    stringbalik=getRest().exchange(URL+"/expertise/add", HttpMethod.POST, requestEntity, String.class).getBody();
                    JOptionPane.showMessageDialog(null,stringbalik);
                }
             } catch (Exception e) {
                 System.out.println("Notif : "+e);
                 if(e.toString().contains("UnknownHostException")||e.toString().contains("404")){
                    JOptionPane.showMessageDialog(null,"Koneksi ke server Jii terputus...!");
                 }
             } finally{
                 if(rs!=null){
                     rs.close();
                 }
                 if(ps!=null){
                     ps.close();
                 }
             }
        } catch (Exception ex) {
            System.out.println("Notifikasi : "+ex);
            if(ex.toString().contains("UnknownHostException")||ex.toString().contains("404")){
                JOptionPane.showMessageDialog(null,"Koneksi ke server Jii terputus...!");
            }
        }
    }
    
    public void HapusRalan(String nopermintaan) {
        try {
             ps=koneksi.prepareStatement(
                    "select permintaan_radiologi.noorder,permintaan_radiologi.no_rawat,reg_periksa.no_rkm_medis,pasien.nm_pasien,permintaan_radiologi.tgl_permintaan,"+
                    "if(permintaan_radiologi.jam_permintaan='00:00:00','',permintaan_radiologi.jam_permintaan) as jam_permintaan,pasien.tgl_lahir,pasien.jk,pasien.alamat,"+
                    "if(permintaan_radiologi.tgl_sampel='0000-00-00','',permintaan_radiologi.tgl_sampel) as tgl_sampel,if(permintaan_radiologi.jam_sampel='00:00:00','',permintaan_radiologi.jam_sampel) as jam_sampel,"+
                    "if(permintaan_radiologi.tgl_hasil='0000-00-00','',permintaan_radiologi.tgl_hasil) as tgl_hasil,if(permintaan_radiologi.jam_hasil='00:00:00','',permintaan_radiologi.jam_hasil) as jam_hasil,"+
                    "permintaan_radiologi.dokter_perujuk,dokter.nm_dokter,poliklinik.nm_poli,pasien.no_tlp,penjab.png_jawab,permintaan_pemeriksaan_radiologi.kd_jenis_prw,jns_perawatan_radiologi.nm_perawatan, "+
                    "permintaan_radiologi.diagnosa_klinis,permintaan_radiologi.informasi_tambahan,kabupaten.nm_kab from permintaan_radiologi "+
                    "inner join reg_periksa on permintaan_radiologi.no_rawat=reg_periksa.no_rawat "+
                    "inner join pasien on reg_periksa.no_rkm_medis=pasien.no_rkm_medis "+
                    "inner join kabupaten ON pasien.kd_kab = kabupaten.kd_kab "+
                    "inner join dokter on permintaan_radiologi.dokter_perujuk=dokter.kd_dokter "+
                    "inner join poliklinik on reg_periksa.kd_poli=poliklinik.kd_poli "+
                    "inner join penjab on reg_periksa.kd_pj=penjab.kd_pj "+
                    "inner join permintaan_pemeriksaan_radiologi on permintaan_pemeriksaan_radiologi.noorder=permintaan_radiologi.noorder "+
                    "inner join jns_perawatan_radiologi on permintaan_pemeriksaan_radiologi.kd_jenis_prw=jns_perawatan_radiologi.kd_jenis_prw "+
                    "where permintaan_radiologi.noorder=? "+
                    "group by permintaan_pemeriksaan_radiologi.kd_jenis_prw");
             try {
                ps.setString(1,nopermintaan);
                rs3=ps.executeQuery();
                 jumlah = 0;
                 while(rs3.next()){
                     jumlah = jumlah + 1;
                } 
                //System.out.println(jumlah);
                ps.setString(1,nopermintaan);
                rs2=ps.executeQuery();
                no_order = new String[jumlah];
                 loop = 0;
                 while(rs2.next()){
                     no_order[loop]= +loop+"\":\""+rs2.getString("noorder")+rs2.getString("kd_jenis_prw").substring(idd);
                     loop = loop + 1;
                }
                // tindakan = hasil[0]+hasil[1]+hasil[3];
                order = Arrays.toString(no_order);
                ps.setString(1,nopermintaan);
                rs=ps.executeQuery();
                    headers = new HttpHeaders();
                    headers.setContentType(MediaType.APPLICATION_JSON);
                    headers.add("Content-Type","application/json;charset=UTF-8");
                    requestJson="{" +      
                                    "\"acc_number\":{"+order.replaceAll(",","\",\"").replaceAll("\\[","\"").replaceAll("\\]","\"\\}").replaceAll(" ","")+
                                "}"; 
                    System.out.println("JSON : "+requestJson);
                    System.out.println("URL : "+URL+"/order/addcancel?");
                    requestEntity = new HttpEntity(requestJson,headers);	    
                    stringbalik=getRest().exchange(URL+"/order/addcancel?", HttpMethod.POST, requestEntity, String.class).getBody();
                    JOptionPane.showMessageDialog(null,stringbalik);
             } catch (Exception e) {
                 System.out.println("Notif : "+e);
                 if(e.toString().contains("UnknownHostException")||e.toString().contains("404")){
                    JOptionPane.showMessageDialog(null,"Koneksi ke server Jii terputus...!");
                 }
             } finally{
                 if(rs!=null){
                     rs.close();
                 }
                 if(ps!=null){
                     ps.close();
                 }
             }
        } catch (Exception ex) {
            System.out.println("Notifikasi : "+ex);
            if(ex.toString().contains("UnknownHostException")||ex.toString().contains("404")){
                JOptionPane.showMessageDialog(null,"Koneksi ke server Jii terputus...!");
            }
        }
    }
    
    public void GambarRalan(String nopermintaan) {
              String os = System.getProperty("os.name").toLowerCase();
              Runtime rt = Runtime.getRuntime();    
        
        try {
             ps=koneksi.prepareStatement(
                    "select permintaan_radiologi.noorder,permintaan_radiologi.no_rawat,reg_periksa.no_rkm_medis,pasien.nm_pasien,permintaan_radiologi.tgl_permintaan,"+
                    "if(permintaan_radiologi.jam_permintaan='00:00:00','',permintaan_radiologi.jam_permintaan) as jam_permintaan,pasien.tgl_lahir,pasien.jk,pasien.alamat,"+
                    "if(permintaan_radiologi.tgl_sampel='0000-00-00','',permintaan_radiologi.tgl_sampel) as tgl_sampel,if(permintaan_radiologi.jam_sampel='00:00:00','',permintaan_radiologi.jam_sampel) as jam_sampel,"+
                    "if(permintaan_radiologi.tgl_hasil='0000-00-00','',permintaan_radiologi.tgl_hasil) as tgl_hasil,if(permintaan_radiologi.jam_hasil='00:00:00','',permintaan_radiologi.jam_hasil) as jam_hasil,"+
                    "permintaan_radiologi.dokter_perujuk,dokter.nm_dokter,poliklinik.nm_poli,pasien.no_tlp,penjab.png_jawab,permintaan_pemeriksaan_radiologi.kd_jenis_prw,jns_perawatan_radiologi.nm_perawatan, "+
                    "permintaan_radiologi.diagnosa_klinis,permintaan_radiologi.informasi_tambahan,kabupaten.nm_kab from permintaan_radiologi "+
                    "inner join reg_periksa on permintaan_radiologi.no_rawat=reg_periksa.no_rawat "+
                    "inner join pasien on reg_periksa.no_rkm_medis=pasien.no_rkm_medis "+
                    "inner join kabupaten ON pasien.kd_kab = kabupaten.kd_kab "+
                    "inner join dokter on permintaan_radiologi.dokter_perujuk=dokter.kd_dokter "+
                    "inner join poliklinik on reg_periksa.kd_poli=poliklinik.kd_poli "+
                    "inner join penjab on reg_periksa.kd_pj=penjab.kd_pj "+
                    "inner join permintaan_pemeriksaan_radiologi on permintaan_pemeriksaan_radiologi.noorder=permintaan_radiologi.noorder "+
                    "inner join jns_perawatan_radiologi on permintaan_pemeriksaan_radiologi.kd_jenis_prw=jns_perawatan_radiologi.kd_jenis_prw "+
                    "where permintaan_radiologi.noorder=? "+
                    "group by permintaan_pemeriksaan_radiologi.kd_jenis_prw");
             try {     
                ps.setString(1,nopermintaan);
                rs3=ps.executeQuery();
                 jumlah = 0;
                 while(rs3.next()){
                     jumlah = jumlah + 1;
                } 
                //System.out.println(jumlah);
                ps.setString(1,nopermintaan);
                rs2=ps.executeQuery();
                no_order = new String[jumlah];
                 loop = 0;
                 while(rs2.next()){
                     no_order[loop]= rs2.getString("noorder")+rs2.getString("kd_jenis_prw").substring(idd);
                     try{ 
                            if(os.contains("win")) {
                                rt.exec( "rundll32 url.dll,FileProtocolHandler " + "https://"+URLHASIL+"?mode=proxy&lights=on&titlebar=on#View&ris_exam_id="+no_order[loop].toString()+"&un="+UN+"&pw="+PW+"");
                            }else if (os.contains("mac")) {
                                rt.exec( "open " + "https://"+URLHASIL+"?mode=proxy&lights=on&titlebar=on#View&ris_exam_id="+no_order[loop].toString()+"&un="+UN+"&pw="+PW+"");
                            }else if (os.contains("nix") || os.contains("nux")) {
                                String[] browsers = {"x-www-browser","epiphany", "firefox", "mozilla", "konqueror","chrome","chromium","netscape","opera","links","lynx","midori"};
                                // Build a command string which looks like "browser1 "url" || browser2 "url" ||..."
                                StringBuilder cmd = new StringBuilder();
                                for(i=0; i<browsers.length; i++) cmd.append(i==0  ? "" : " || ").append(browsers[i]).append(" \"").append("https://"+URLHASIL+"?mode=proxy&lights=on&titlebar=on#View&ris_exam_id="+no_order[loop].toString()+"&&un="+UN+"&pw="+PW+"").append( "\" ");
                                rt.exec(new String[] { "sh", "-c", cmd.toString() });
                            } 
                        }catch (Exception e){
                            System.out.println("Notif Browser : "+e);
                        } 
                     loop = loop + 1;
                }

             } catch (Exception e) {
                 System.out.println("Notif : "+e);
                 if(e.toString().contains("UnknownHostException")||e.toString().contains("404")){
                    JOptionPane.showMessageDialog(null,"Koneksi ke server Jii terputus...!");
                 }
             } finally{
                 if(rs!=null){
                     rs.close();
                 }
                 if(ps!=null){
                     ps.close();
                 }
             }
        } catch (Exception ex) {
            System.out.println("Notifikasi : "+ex);
            if(ex.toString().contains("UnknownHostException")||ex.toString().contains("404")){
                JOptionPane.showMessageDialog(null,"Koneksi ke server Jii terputus...!");
            }
        }
    }
    
    public void GambarRalanAll(String nopermintaan) {
              String os = System.getProperty("os.name").toLowerCase();
              Runtime rt = Runtime.getRuntime();    
        
        try {
             ps=koneksi.prepareStatement(
                    "select permintaan_radiologi.noorder,permintaan_radiologi.no_rawat,reg_periksa.no_rkm_medis,pasien.nm_pasien,permintaan_radiologi.tgl_permintaan,"+
                    "if(permintaan_radiologi.jam_permintaan='00:00:00','',permintaan_radiologi.jam_permintaan) as jam_permintaan,pasien.tgl_lahir,pasien.jk,pasien.alamat,"+
                    "if(permintaan_radiologi.tgl_sampel='0000-00-00','',permintaan_radiologi.tgl_sampel) as tgl_sampel,if(permintaan_radiologi.jam_sampel='00:00:00','',permintaan_radiologi.jam_sampel) as jam_sampel,"+
                    "if(permintaan_radiologi.tgl_hasil='0000-00-00','',permintaan_radiologi.tgl_hasil) as tgl_hasil,if(permintaan_radiologi.jam_hasil='00:00:00','',permintaan_radiologi.jam_hasil) as jam_hasil,"+
                    "permintaan_radiologi.dokter_perujuk,dokter.nm_dokter,poliklinik.nm_poli,pasien.no_tlp,penjab.png_jawab,permintaan_pemeriksaan_radiologi.kd_jenis_prw,jns_perawatan_radiologi.nm_perawatan, "+
                    "permintaan_radiologi.diagnosa_klinis,permintaan_radiologi.informasi_tambahan,kabupaten.nm_kab from permintaan_radiologi "+
                    "inner join reg_periksa on permintaan_radiologi.no_rawat=reg_periksa.no_rawat "+
                    "inner join pasien on reg_periksa.no_rkm_medis=pasien.no_rkm_medis "+
                    "inner join kabupaten ON pasien.kd_kab = kabupaten.kd_kab "+
                    "inner join dokter on permintaan_radiologi.dokter_perujuk=dokter.kd_dokter "+
                    "inner join poliklinik on reg_periksa.kd_poli=poliklinik.kd_poli "+
                    "inner join penjab on reg_periksa.kd_pj=penjab.kd_pj "+
                    "inner join permintaan_pemeriksaan_radiologi on permintaan_pemeriksaan_radiologi.noorder=permintaan_radiologi.noorder "+
                    "inner join jns_perawatan_radiologi on permintaan_pemeriksaan_radiologi.kd_jenis_prw=jns_perawatan_radiologi.kd_jenis_prw "+
                    "where permintaan_radiologi.noorder=? "+
                    "group by permintaan_pemeriksaan_radiologi.kd_jenis_prw");
             try {     

                //System.out.println(jumlah);
                ps.setString(1,nopermintaan);
                rs=ps.executeQuery();
                while(rs.next()){
                    try{ 
                            if(os.contains("win")) {
                                rt.exec( "rundll32 url.dll,FileProtocolHandler " + "https://"+URLHASIL+"?mode=proxy&lights=on&titlebar=on#View&ris_pat_id="+rs.getString("no_rkm_medis")+"&un="+UN+"&pw="+PW+"");
                            }else if (os.contains("mac")) {
                                rt.exec( "open " + "https://"+URLHASIL+"?mode=proxy&lights=on&titlebar=on#View&ris_pat_id="+rs.getString("no_rkm_medis")+"&un="+UN+"&pw="+PW+"");
                            }else if (os.contains("nix") || os.contains("nux")) {
                                String[] browsers = {"x-www-browser","epiphany", "firefox", "mozilla", "konqueror","chrome","chromium","netscape","opera","links","lynx","midori"};
                                // Build a command string which looks like "browser1 "url" || browser2 "url" ||..."
                                StringBuilder cmd = new StringBuilder();
                                for(i=0; i<browsers.length; i++) cmd.append(i==0  ? "" : " || ").append(browsers[i]).append(" \"").append("https://"+URLHASIL+"?mode=proxy&lights=on&titlebar=on#View&ris_pat_id="+rs.getString("no_rkm_medis")+"&un="+UN+"&pw="+PW+"").append( "\" ");
                                rt.exec(new String[] { "sh", "-c", cmd.toString() });
                            } 
                        }catch (Exception e){
                            System.out.println("Notif Browser : "+e);
                        } 
                }

             } catch (Exception e) {
                 System.out.println("Notif : "+e);
                 if(e.toString().contains("UnknownHostException")||e.toString().contains("404")){
                    JOptionPane.showMessageDialog(null,"Koneksi ke server Jii terputus...!");
                 }
             } finally{
                 if(rs!=null){
                     rs.close();
                 }
                 if(ps!=null){
                     ps.close();
                 }
             }
        } catch (Exception ex) {
            System.out.println("Notifikasi : "+ex);
            if(ex.toString().contains("UnknownHostException")||ex.toString().contains("404")){
                JOptionPane.showMessageDialog(null,"Koneksi ke server Jii terputus...!");
            }
        }
    }
    
    public void kirimRanap(String nopermintaan) {
        try {
             ps=koneksi.prepareStatement(
                    "select permintaan_radiologi.noorder,permintaan_radiologi.no_rawat,reg_periksa.no_rkm_medis,pasien.nm_pasien,permintaan_radiologi.tgl_permintaan,"+
                    "if(permintaan_radiologi.jam_permintaan='00:00:00','',permintaan_radiologi.jam_permintaan) as jam_permintaan,pasien.jk,pasien.alamat,"+
                    "if(permintaan_radiologi.tgl_sampel='0000-00-00','',permintaan_radiologi.tgl_sampel) as tgl_sampel,if(permintaan_radiologi.jam_sampel='00:00:00','',permintaan_radiologi.jam_sampel) as jam_sampel,"+
                    "if(permintaan_radiologi.tgl_hasil='0000-00-00','',permintaan_radiologi.tgl_hasil) as tgl_hasil,if(permintaan_radiologi.jam_hasil='00:00:00','',permintaan_radiologi.jam_hasil) as jam_hasil,"+
                    "permintaan_radiologi.dokter_perujuk,dokter.nm_dokter,bangsal.nm_bangsal,pasien.no_tlp,penjab.png_jawab,pasien.tgl_lahir,permintaan_pemeriksaan_radiologi.kd_jenis_prw,jns_perawatan_radiologi.nm_perawatan, "+
                    "permintaan_radiologi.diagnosa_klinis,permintaan_radiologi.informasi_tambahan,kabupaten.nm_kab from permintaan_radiologi "+
                    "inner join reg_periksa on permintaan_radiologi.no_rawat=reg_periksa.no_rawat "+
                    "inner join pasien on reg_periksa.no_rkm_medis=pasien.no_rkm_medis "+
                    "inner join kabupaten ON pasien.kd_kab = kabupaten.kd_kab "+
                    "inner join dokter on permintaan_radiologi.dokter_perujuk=dokter.kd_dokter "+
                    "inner join kamar_inap on reg_periksa.no_rawat=kamar_inap.no_rawat "+
                    "inner join kamar on kamar_inap.kd_kamar=kamar.kd_kamar "+
                    "inner join bangsal on kamar.kd_bangsal=bangsal.kd_bangsal "+
                    "inner join penjab on reg_periksa.kd_pj=penjab.kd_pj "+
                    "inner join permintaan_pemeriksaan_radiologi on permintaan_pemeriksaan_radiologi.noorder=permintaan_radiologi.noorder "+
                    "inner join jns_perawatan_radiologi on permintaan_pemeriksaan_radiologi.kd_jenis_prw=jns_perawatan_radiologi.kd_jenis_prw "+
                    "where permintaan_radiologi.noorder=? "+
                    "group by permintaan_pemeriksaan_radiologi.kd_jenis_prw");
             try {
                ps.setString(1,nopermintaan);
                rs3=ps.executeQuery();
                 jumlah = 0;
                 while(rs3.next()){
                     jumlah = jumlah + 1;
                } 
                //System.out.println(jumlah);
                ps.setString(1,nopermintaan);
                rs2=ps.executeQuery();
                nm_tindakan = new String[jumlah];
                no_order = new String[jumlah];
                mdl = new String[jumlah];
                 loop = 0;
                 while(rs2.next()){
                     no_order[loop]= rs2.getString("noorder")+rs2.getString("kd_jenis_prw").substring(idd);
                     nm_tindakan[loop]= rs2.getString("nm_perawatan");
                     mdl[loop]= rs2.getString("kd_jenis_prw").substring(0,2);
                     loop = loop + 1;
                }
                // tindakan = hasil[0]+hasil[1]+hasil[3];
                order = Arrays.toString(no_order);
                tindakan = Arrays.toString(nm_tindakan);
                modal = Arrays.toString(mdl);
                ps.setString(1,nopermintaan);
                rs=ps.executeQuery();
                while(rs.next()){
                    headers = new HttpHeaders();
                    headers.setContentType(MediaType.APPLICATION_JSON);
                    headers.add("Content-Type","application/json;charset=UTF-8");
                    requestJson="{" +
                                    "\"no_rkm_medis\": \""+rs.getString("no_rkm_medis")+"\"," +
                                    "\"nm_pasien\": \""+rs.getString("nm_pasien")+"\"," +
                                    "\"tgl_lahir\": \""+rs.getString("tgl_lahir")+"\"," +
                                    "\"sex\": \""+rs.getString("jk").replaceAll("L","M").replaceAll("P","F")+"\"," +
                                    "\"alamat\": \""+rs.getString("alamat")+"\"," +
                                    "\"kota\": \""+rs.getString("nm_kab")+"\"," +
                                    "\"nm_dokter\": \""+rs.getString("nm_dokter")+"\"," +
                                    "\"nm_poli\": \""+rs.getString("nm_bangsal")+"\"," +
                                    "\"informasi_tambahan\": \""+(rs.getString("informasi_tambahan").toLowerCase().contains("cito")?"CITO":"")+"\"," +
                                    "\"tgl_periksa\": \""+rs.getString("tgl_permintaan")+" "+rs.getString("jam_permintaan")+"\"," +
                                    "\"acc_number\": "+order.replaceAll(",","\",\"").replaceAll("\\[","\\[\"").replaceAll("\\]","\"\\]")+"," +
                                    "\"nm_perawatan\": "+tindakan.replaceAll(",","\",\"").replaceAll("\\[","\\[\"").replaceAll("\\]","\"\\]")+"," +
                                    "\"modality\": "+modal.replaceAll(",","\",\"").replaceAll("\\[","\\[\"").replaceAll("\\]","\"\\]") +
                                "}";  
                    System.out.println("JSON : "+requestJson);
                    System.out.println("URL : "+URL+"/order/add");
                    requestEntity = new HttpEntity(requestJson,headers);	    
                    stringbalik=getRest().exchange(URL+"/order/add", HttpMethod.POST, requestEntity, String.class).getBody();
                    JOptionPane.showMessageDialog(null,stringbalik);
                }
             } catch (Exception e) {
                 System.out.println("Notif : "+e);
                 if(e.toString().contains("UnknownHostException")||e.toString().contains("404")){
                    JOptionPane.showMessageDialog(null,"Koneksi ke server Jii terputus...!");
                 }
             } finally{
                 if(rs!=null){
                     rs.close();
                 }
                 if(ps!=null){
                     ps.close();
                 }
             }
        } catch (Exception ex) {
            System.out.println("Notifikasi : "+ex);
            if(ex.toString().contains("UnknownHostException")||ex.toString().contains("404")){
                JOptionPane.showMessageDialog(null,"Koneksi ke server Jii terputus...!");
            }
        }
    }
    
    public void HapusRanap(String nopermintaan) {
        try {
             ps=koneksi.prepareStatement(
                    "select permintaan_radiologi.noorder,permintaan_radiologi.no_rawat,reg_periksa.no_rkm_medis,pasien.nm_pasien,permintaan_radiologi.tgl_permintaan,"+
                    "if(permintaan_radiologi.jam_permintaan='00:00:00','',permintaan_radiologi.jam_permintaan) as jam_permintaan,pasien.jk,pasien.alamat,"+
                    "if(permintaan_radiologi.tgl_sampel='0000-00-00','',permintaan_radiologi.tgl_sampel) as tgl_sampel,if(permintaan_radiologi.jam_sampel='00:00:00','',permintaan_radiologi.jam_sampel) as jam_sampel,"+
                    "if(permintaan_radiologi.tgl_hasil='0000-00-00','',permintaan_radiologi.tgl_hasil) as tgl_hasil,if(permintaan_radiologi.jam_hasil='00:00:00','',permintaan_radiologi.jam_hasil) as jam_hasil,"+
                    "permintaan_radiologi.dokter_perujuk,dokter.nm_dokter,bangsal.nm_bangsal,pasien.no_tlp,penjab.png_jawab,pasien.tgl_lahir,permintaan_pemeriksaan_radiologi.kd_jenis_prw,jns_perawatan_radiologi.nm_perawatan, "+
                    "permintaan_radiologi.diagnosa_klinis,permintaan_radiologi.informasi_tambahan,kabupaten.nm_kab from permintaan_radiologi "+
                    "inner join reg_periksa on permintaan_radiologi.no_rawat=reg_periksa.no_rawat "+
                    "inner join pasien on reg_periksa.no_rkm_medis=pasien.no_rkm_medis "+
                    "inner join kabupaten ON pasien.kd_kab = kabupaten.kd_kab "+
                    "inner join dokter on permintaan_radiologi.dokter_perujuk=dokter.kd_dokter "+
                    "inner join kamar_inap on reg_periksa.no_rawat=kamar_inap.no_rawat "+
                    "inner join kamar on kamar_inap.kd_kamar=kamar.kd_kamar "+
                    "inner join bangsal on kamar.kd_bangsal=bangsal.kd_bangsal "+
                    "inner join penjab on reg_periksa.kd_pj=penjab.kd_pj "+
                    "inner join permintaan_pemeriksaan_radiologi on permintaan_pemeriksaan_radiologi.noorder=permintaan_radiologi.noorder "+
                    "inner join jns_perawatan_radiologi on permintaan_pemeriksaan_radiologi.kd_jenis_prw=jns_perawatan_radiologi.kd_jenis_prw "+
                    "where permintaan_radiologi.noorder=? "+
                    "group by permintaan_pemeriksaan_radiologi.kd_jenis_prw");
             try {
                ps.setString(1,nopermintaan);
                rs3=ps.executeQuery();
                 jumlah = 0;
                 while(rs3.next()){
                     jumlah = jumlah + 1;
                } 
                //System.out.println(jumlah);
                ps.setString(1,nopermintaan);
                rs2=ps.executeQuery();
                no_order = new String[jumlah];
                 loop = 0;
                 while(rs2.next()){
                     no_order[loop]= +loop+"\":\""+rs2.getString("noorder")+rs2.getString("kd_jenis_prw").substring(idd);
                     loop = loop + 1;
                }
                // tindakan = hasil[0]+hasil[1]+hasil[3];
                order = Arrays.toString(no_order);
                ps.setString(1,nopermintaan);
                rs=ps.executeQuery();
                    headers = new HttpHeaders();
                    headers.setContentType(MediaType.APPLICATION_JSON);
                    headers.add("Content-Type","application/json;charset=UTF-8");
                    requestJson="{" +      
                                    "\"acc_number\":{"+order.replaceAll(",","\",\"").replaceAll("\\[","\"").replaceAll("\\]","\"\\}").replaceAll(" ","")+
                                "}";
                    System.out.println("JSON : "+requestJson);
                    System.out.println("URL : "+URL+"/order/addcancel?");
                    requestEntity = new HttpEntity(requestJson,headers);	    
                    stringbalik=getRest().exchange(URL+"/order/addcancel?", HttpMethod.POST, requestEntity, String.class).getBody();
                    JOptionPane.showMessageDialog(null,stringbalik);
             } catch (Exception e) {
                 System.out.println("Notif : "+e);
                 if(e.toString().contains("UnknownHostException")||e.toString().contains("404")){
                    JOptionPane.showMessageDialog(null,"Koneksi ke server Jii terputus...!");
                 }
             } finally{
                 if(rs!=null){
                     rs.close();
                 }
                 if(ps!=null){
                     ps.close();
                 }
             }
        } catch (Exception ex) {
            System.out.println("Notifikasi : "+ex);
            if(ex.toString().contains("UnknownHostException")||ex.toString().contains("404")){
                JOptionPane.showMessageDialog(null,"Koneksi ke server Jii terputus...!");
            }
        }
    }
    
    public void ambilRanap(String nopermintaan) {
        try {
             ps=koneksi.prepareStatement(
                    "select permintaan_radiologi.noorder,permintaan_radiologi.no_rawat,reg_periksa.no_rkm_medis,pasien.nm_pasien,permintaan_radiologi.tgl_permintaan,"+
                    "if(permintaan_radiologi.jam_permintaan='00:00:00','',permintaan_radiologi.jam_permintaan) as jam_permintaan,pasien.jk,pasien.alamat,"+
                    "if(permintaan_radiologi.tgl_sampel='0000-00-00','',permintaan_radiologi.tgl_sampel) as tgl_sampel,if(permintaan_radiologi.jam_sampel='00:00:00','',permintaan_radiologi.jam_sampel) as jam_sampel,"+
                    "if(permintaan_radiologi.tgl_hasil='0000-00-00','',permintaan_radiologi.tgl_hasil) as tgl_hasil,if(permintaan_radiologi.jam_hasil='00:00:00','',permintaan_radiologi.jam_hasil) as jam_hasil,"+
                    "permintaan_radiologi.dokter_perujuk,dokter.nm_dokter,bangsal.nm_bangsal,pasien.no_tlp,penjab.png_jawab,pasien.tgl_lahir,permintaan_pemeriksaan_radiologi.kd_jenis_prw,jns_perawatan_radiologi.nm_perawatan, "+
                    "permintaan_radiologi.diagnosa_klinis,permintaan_radiologi.informasi_tambahan,kabupaten.nm_kab from permintaan_radiologi "+
                    "inner join reg_periksa on permintaan_radiologi.no_rawat=reg_periksa.no_rawat "+
                    "inner join pasien on reg_periksa.no_rkm_medis=pasien.no_rkm_medis "+
                    "inner join kabupaten ON pasien.kd_kab = kabupaten.kd_kab "+
                    "inner join dokter on permintaan_radiologi.dokter_perujuk=dokter.kd_dokter "+
                    "inner join kamar_inap on reg_periksa.no_rawat=kamar_inap.no_rawat "+
                    "inner join kamar on kamar_inap.kd_kamar=kamar.kd_kamar "+
                    "inner join bangsal on kamar.kd_bangsal=bangsal.kd_bangsal "+
                    "inner join penjab on reg_periksa.kd_pj=penjab.kd_pj "+
                    "inner join permintaan_pemeriksaan_radiologi on permintaan_pemeriksaan_radiologi.noorder=permintaan_radiologi.noorder "+
                    "inner join jns_perawatan_radiologi on permintaan_pemeriksaan_radiologi.kd_jenis_prw=jns_perawatan_radiologi.kd_jenis_prw "+
                    "where permintaan_radiologi.noorder=? "+
                    "group by permintaan_pemeriksaan_radiologi.kd_jenis_prw");
             
             try {
                ps.setString(1,nopermintaan);
                rs3=ps.executeQuery();
                 jumlah = 0;
                 while(rs3.next()){
                     jumlah = jumlah + 1;
                } 
                //System.out.println(jumlah);
                ps.setString(1,nopermintaan);
                rs2=ps.executeQuery();
                nm_tindakan = new String[jumlah];
                no_order = new String[jumlah];
                mdl = new String[jumlah];
                 loop = 0;
                 while(rs2.next()){
                     no_order[loop]= +loop+"\":\""+rs2.getString("noorder")+rs2.getString("kd_jenis_prw").substring(idd);
                     nm_tindakan[loop]= +loop+"\":\""+rs2.getString("nm_perawatan");
                     mdl[loop]= +loop+"\":\""+rs2.getString("kd_jenis_prw").substring(0,2);
                     loop = loop + 1;
                }
                // tindakan = hasil[0]+hasil[1]+hasil[3];
                order = Arrays.toString(no_order);
                tindakan = Arrays.toString(nm_tindakan);
                modal = Arrays.toString(mdl);
                ps.setString(1,nopermintaan);
                rs=ps.executeQuery();
                while(rs.next()){
                    headers = new HttpHeaders();
                    headers.setContentType(MediaType.APPLICATION_JSON);
                    headers.add("Content-Type","application/json;charset=UTF-8");
                    requestJson="{" +      
                                    "\"acc_number\":{"+order.replaceAll(",","\",\"").replaceAll("\\[","\"").replaceAll("\\]","\"\\},").replaceAll(" ","")+
                                    "\"no_rkm_medis\": \""+rs.getString("no_rkm_medis")+"\"," +
                                    "\"nm_pasien\": \""+rs.getString("nm_pasien")+"\"," +
                                    "\"tgl_lahir\": \""+rs.getString("tgl_lahir")+"\"," +
                                    "\"sex\": \""+rs.getString("jk").replaceAll("L","M").replaceAll("P","F")+"\"," +
                                    "\"alamat\": \""+rs.getString("alamat")+"\"," +
                                    "\"kota\": \""+rs.getString("nm_kab")+"\"," +
                                    "\"nama_dokter_baca\": \""+Sequel.cariIsi("select dokter.nm_dokter from periksa_radiologi inner join dokter on periksa_radiologi.kd_dokter = dokter.kd_dokter where periksa_radiologi.no_rawat=? and periksa_radiologi.tgl_periksa=\""+rs.getString("tgl_hasil")+"\"and periksa_radiologi.jam=\""+rs.getString("jam_hasil")+"\"",rs.getString("no_rawat"))+"\"," +
                                    "\"nm_perawatan\":{"+tindakan.replaceAll(",","\",\"").replaceAll("\\[","\"").replaceAll("\\]","\"\\},").replaceAll(" ","") +
                                    "\"nm_poli\": \""+rs.getString("nm_bangsal")+"\"," +
                                    "\"modality\":{"+modal.replaceAll(",","\",\"").replaceAll("\\[","\"").replaceAll("\\]","\"\\},").replaceAll(" ","") +
                                    "\"tgl_periksa\": \""+rs.getString("tgl_permintaan")+" "+rs.getString("jam_permintaan")+"\"," +
                                    "\"keterangan\": \""+Sequel.cariIsi("select hasil_radiologi.hasil from hasil_radiologi where hasil_radiologi.no_rawat=? and hasil_radiologi.tgl_periksa=\""+rs.getString("tgl_hasil")+"\"and hasil_radiologi.jam=\""+rs.getString("jam_hasil")+"\"",rs.getString("no_rawat"))+"\"" +         
                                "}"; 
                    System.out.println("JSON : "+requestJson);
                    System.out.println("URL : "+URL+"/expertise/add");
                    requestEntity = new HttpEntity(requestJson,headers);	    
                    stringbalik=getRest().exchange(URL+"/expertise/add", HttpMethod.POST, requestEntity, String.class).getBody();
                    JOptionPane.showMessageDialog(null,stringbalik);
                    //Sequel.cariIsi("select hasil_radiologi.hasil from hasil_radiologi where hasil_radiologi.no_rawat=? order by hasil_radiologi.jam DESC limit 1",rs.getString("no_rawat"))
                }
             } catch (Exception e) {
                 System.out.println("Notif : "+e);
                 if(e.toString().contains("UnknownHostException")||e.toString().contains("404")){
                    JOptionPane.showMessageDialog(null,"Koneksi ke server Jii terputus...!");
                 }
             } finally{
                 if(rs!=null){
                     rs.close();
                 }
                 if(ps!=null){
                     ps.close();
                 }
             }
        } catch (Exception ex) {
            System.out.println("Notifikasi : "+ex);
            if(ex.toString().contains("UnknownHostException")||ex.toString().contains("404")){
                JOptionPane.showMessageDialog(null,"Koneksi ke server Jii terputus...!");
            }
        }
    }
    
    public void GambarRanap(String nopermintaan) {
              String os = System.getProperty("os.name").toLowerCase();
              Runtime rt = Runtime.getRuntime();  
        try {
             ps=koneksi.prepareStatement(
                    "select permintaan_radiologi.noorder,permintaan_radiologi.no_rawat,reg_periksa.no_rkm_medis,pasien.nm_pasien,permintaan_radiologi.tgl_permintaan,"+
                    "if(permintaan_radiologi.jam_permintaan='00:00:00','',permintaan_radiologi.jam_permintaan) as jam_permintaan,pasien.jk,pasien.alamat,"+
                    "if(permintaan_radiologi.tgl_sampel='0000-00-00','',permintaan_radiologi.tgl_sampel) as tgl_sampel,if(permintaan_radiologi.jam_sampel='00:00:00','',permintaan_radiologi.jam_sampel) as jam_sampel,"+
                    "if(permintaan_radiologi.tgl_hasil='0000-00-00','',permintaan_radiologi.tgl_hasil) as tgl_hasil,if(permintaan_radiologi.jam_hasil='00:00:00','',permintaan_radiologi.jam_hasil) as jam_hasil,"+
                    "permintaan_radiologi.dokter_perujuk,dokter.nm_dokter,bangsal.nm_bangsal,pasien.no_tlp,penjab.png_jawab,pasien.tgl_lahir,permintaan_pemeriksaan_radiologi.kd_jenis_prw,jns_perawatan_radiologi.nm_perawatan, "+
                    "permintaan_radiologi.diagnosa_klinis,permintaan_radiologi.informasi_tambahan,kabupaten.nm_kab from permintaan_radiologi "+
                    "inner join reg_periksa on permintaan_radiologi.no_rawat=reg_periksa.no_rawat "+
                    "inner join pasien on reg_periksa.no_rkm_medis=pasien.no_rkm_medis "+
                    "inner join kabupaten ON pasien.kd_kab = kabupaten.kd_kab "+
                    "inner join dokter on permintaan_radiologi.dokter_perujuk=dokter.kd_dokter "+
                    "inner join kamar_inap on reg_periksa.no_rawat=kamar_inap.no_rawat "+
                    "inner join kamar on kamar_inap.kd_kamar=kamar.kd_kamar "+
                    "inner join bangsal on kamar.kd_bangsal=bangsal.kd_bangsal "+
                    "inner join penjab on reg_periksa.kd_pj=penjab.kd_pj "+
                    "inner join permintaan_pemeriksaan_radiologi on permintaan_pemeriksaan_radiologi.noorder=permintaan_radiologi.noorder "+
                    "inner join jns_perawatan_radiologi on permintaan_pemeriksaan_radiologi.kd_jenis_prw=jns_perawatan_radiologi.kd_jenis_prw "+
                    "where permintaan_radiologi.noorder=? "+
                    "group by permintaan_pemeriksaan_radiologi.kd_jenis_prw");
             try {
                ps.setString(1,nopermintaan);
                rs3=ps.executeQuery();
                 jumlah = 0;
                 while(rs3.next()){
                     jumlah = jumlah + 1;
                } 
                //System.out.println(jumlah);
                ps.setString(1,nopermintaan);
                rs2=ps.executeQuery();
                no_order = new String[jumlah];
                 loop = 0;
                 while(rs2.next()){
                     no_order[loop]= rs2.getString("noorder")+rs2.getString("kd_jenis_prw").substring(idd);
                        try{ 
                            if(os.contains("win")) {
                                rt.exec( "rundll32 url.dll,FileProtocolHandler " + "https://"+URLHASIL+"?mode=proxy&lights=on&titlebar=on#View&ris_exam_id="+no_order[loop].toString()+"&un="+UN+"&pw="+PW+"");
                            }else if (os.contains("mac")) {
                                rt.exec( "open " + "https://"+URLHASIL+"?mode=proxy&lights=on&titlebar=on#View&ris_exam_id="+no_order[loop].toString()+"&un="+UN+"&pw="+PW+"");
                            }else if (os.contains("nix") || os.contains("nux")) {
                                String[] browsers = {"x-www-browser","epiphany", "firefox", "mozilla", "konqueror","chrome","chromium","netscape","opera","links","lynx","midori"};
                                // Build a command string which looks like "browser1 "url" || browser2 "url" ||..."
                                StringBuilder cmd = new StringBuilder();
                                for(i=0; i<browsers.length; i++) cmd.append(i==0  ? "" : " || ").append(browsers[i]).append(" \"").append("https://"+URLHASIL+"?mode=proxy&lights=on&titlebar=on#View&ris_exam_id="+no_order[loop].toString()+"&un="+UN+"&pw="+PW+"").append( "\" ");
                                rt.exec(new String[] { "sh", "-c", cmd.toString() });
                            } 
                        }catch (Exception e){
                            System.out.println("Notif Browser : "+e);
                        } 
                     loop = loop + 1;
                }

             } catch (Exception e) {
                 System.out.println("Notif : "+e);
                 if(e.toString().contains("UnknownHostException")||e.toString().contains("404")){
                    JOptionPane.showMessageDialog(null,"Koneksi ke server Jii terputus...!");
                 }
             } finally{
                 if(rs!=null){
                     rs.close();
                 }
                 if(ps!=null){
                     ps.close();
                 }
             }
        } catch (Exception ex) {
            System.out.println("Notifikasi : "+ex);
            if(ex.toString().contains("UnknownHostException")||ex.toString().contains("404")){
                JOptionPane.showMessageDialog(null,"Koneksi ke server Jii terputus...!");
            }
        }
    }
    
            public void GambarRanapAll(String nopermintaan) {
              String os = System.getProperty("os.name").toLowerCase();
              Runtime rt = Runtime.getRuntime();  
        try {
             ps=koneksi.prepareStatement(
                    "select permintaan_radiologi.noorder,permintaan_radiologi.no_rawat,reg_periksa.no_rkm_medis,pasien.nm_pasien,permintaan_radiologi.tgl_permintaan,"+
                    "if(permintaan_radiologi.jam_permintaan='00:00:00','',permintaan_radiologi.jam_permintaan) as jam_permintaan,pasien.jk,pasien.alamat,"+
                    "if(permintaan_radiologi.tgl_sampel='0000-00-00','',permintaan_radiologi.tgl_sampel) as tgl_sampel,if(permintaan_radiologi.jam_sampel='00:00:00','',permintaan_radiologi.jam_sampel) as jam_sampel,"+
                    "if(permintaan_radiologi.tgl_hasil='0000-00-00','',permintaan_radiologi.tgl_hasil) as tgl_hasil,if(permintaan_radiologi.jam_hasil='00:00:00','',permintaan_radiologi.jam_hasil) as jam_hasil,"+
                    "permintaan_radiologi.dokter_perujuk,dokter.nm_dokter,bangsal.nm_bangsal,pasien.no_tlp,penjab.png_jawab,pasien.tgl_lahir,permintaan_pemeriksaan_radiologi.kd_jenis_prw,jns_perawatan_radiologi.nm_perawatan, "+
                    "permintaan_radiologi.diagnosa_klinis,permintaan_radiologi.informasi_tambahan,kabupaten.nm_kab from permintaan_radiologi "+
                    "inner join reg_periksa on permintaan_radiologi.no_rawat=reg_periksa.no_rawat "+
                    "inner join pasien on reg_periksa.no_rkm_medis=pasien.no_rkm_medis "+
                    "inner join kabupaten ON pasien.kd_kab = kabupaten.kd_kab "+
                    "inner join dokter on permintaan_radiologi.dokter_perujuk=dokter.kd_dokter "+
                    "inner join kamar_inap on reg_periksa.no_rawat=kamar_inap.no_rawat "+
                    "inner join kamar on kamar_inap.kd_kamar=kamar.kd_kamar "+
                    "inner join bangsal on kamar.kd_bangsal=bangsal.kd_bangsal "+
                    "inner join penjab on reg_periksa.kd_pj=penjab.kd_pj "+
                    "inner join permintaan_pemeriksaan_radiologi on permintaan_pemeriksaan_radiologi.noorder=permintaan_radiologi.noorder "+
                    "inner join jns_perawatan_radiologi on permintaan_pemeriksaan_radiologi.kd_jenis_prw=jns_perawatan_radiologi.kd_jenis_prw "+
                    "where permintaan_radiologi.noorder=? "+
                    "group by permintaan_pemeriksaan_radiologi.kd_jenis_prw");
             try {
                                //System.out.println(jumlah);
                ps.setString(1,nopermintaan);
                rs=ps.executeQuery();
                while(rs.next()){
                    try{ 
                            if(os.contains("win")) {
                                rt.exec( "rundll32 url.dll,FileProtocolHandler " + "https://"+URLHASIL+"?mode=proxy&lights=on&titlebar=on#View&ris_pat_id="+rs.getString("no_rkm_medis")+"&un="+UN+"&pw="+PW+"");
                            }else if (os.contains("mac")) {
                                rt.exec( "open " + "https://"+URLHASIL+"?mode=proxy&lights=on&titlebar=on#View&ris_pat_id="+rs.getString("no_rkm_medis")+"&un="+UN+"&pw="+PW+"");
                            }else if (os.contains("nix") || os.contains("nux")) {
                                String[] browsers = {"x-www-browser","epiphany", "firefox", "mozilla", "konqueror","chrome","chromium","netscape","opera","links","lynx","midori"};
                                // Build a command string which looks like "browser1 "url" || browser2 "url" ||..."
                                StringBuilder cmd = new StringBuilder();
                                for(i=0; i<browsers.length; i++) cmd.append(i==0  ? "" : " || ").append(browsers[i]).append(" \"").append("https://"+URLHASIL+"?mode=proxy&lights=on&titlebar=on#View&ris_pat_id="+rs.getString("no_rkm_medis")+"&un="+UN+"&pw="+PW+"").append( "\" ");
                                rt.exec(new String[] { "sh", "-c", cmd.toString() });
                            } 
                        }catch (Exception e){
                            System.out.println("Notif Browser : "+e);
                        } 
                }

             } catch (Exception e) {
                 System.out.println("Notif : "+e);
                 if(e.toString().contains("UnknownHostException")||e.toString().contains("404")){
                    JOptionPane.showMessageDialog(null,"Koneksi ke server Jii terputus...!");
                 }
             } finally{
                 if(rs!=null){
                     rs.close();
                 }
                 if(ps!=null){
                     ps.close();
                 }
             }
        } catch (Exception ex) {
            System.out.println("Notifikasi : "+ex);
            if(ex.toString().contains("UnknownHostException")||ex.toString().contains("404")){
                JOptionPane.showMessageDialog(null,"Koneksi ke server Jii terputus...!");
            }
        }
    }
    public RestTemplate getRest() throws NoSuchAlgorithmException, KeyManagementException {
        SSLContext sslContext = SSLContext.getInstance("SSL");
        javax.net.ssl.TrustManager[] trustManagers= {
            new X509TrustManager() {
                public X509Certificate[] getAcceptedIssuers() {return null;}
                public void checkServerTrusted(X509Certificate[] arg0, String arg1)throws CertificateException {}
                public void checkClientTrusted(X509Certificate[] arg0, String arg1)throws CertificateException {}
            }
        };
        sslContext.init(null,trustManagers , new SecureRandom());
        SSLSocketFactory sslFactory=new SSLSocketFactory(sslContext,SSLSocketFactory.ALLOW_ALL_HOSTNAME_VERIFIER);
        Scheme scheme=new Scheme("https",443,sslFactory);
        HttpComponentsClientHttpRequestFactory factory=new HttpComponentsClientHttpRequestFactory();
        factory.getHttpClient().getConnectionManager().getSchemeRegistry().register(scheme);
        return new RestTemplate(factory);
    }
    
}
