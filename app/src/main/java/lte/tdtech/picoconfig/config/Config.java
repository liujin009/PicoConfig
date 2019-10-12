package lte.tdtech.picoconfig.config;

import lte.tdtech.picoconfig.PicApplication;
import lte.tdtech.picoconfig.R;
import lte.tdtech.picoconfig.util.SshServerUtils;

/**
 * Created by lWX453051 on 2019/5/5.
 */

public class Config {

/*    public static final String testhost = "10.17.84.180";
    public static final int testport = 22;
    public static final String testuserid = "ubp";
    public static final String testpwd = "eLTE@com123";*/

/*    public static final String testhost = "10.17.84.180";
    public static final int testport = 830;
    public static final String testuserid = "emscomm";
    public static final String testpwd = "ei*b+@b#6Nh(tS1j";*/

    /**
     * data type
     */
    public static final int Sshquerycmd_LST_IPINTERFACE = 1;
    public static final int Sshquerycmd_LST_CELL = 2;

    /**
     * Event
     */
    public static final String UserLoginEvent = "UserLoginEvent";
    public static final String Userdisconnected = "Userdisconnected";
    public static final String UsernotAuthenticated = "UsernotAuthenticated";

    public static final String RoutequeryEvent = "RoutequeryEvent";
    public static final String RoutemodifiedEvent = "RoutemodifiedEvent";

    public static final String IpqueryEvent = "IpqueryEvent";
    public static final String IpmodifiedEvent = "IpmodifiedEvent";

    public static final String FrequeryEvent = "FrequeryEvent";
    public static final String FremodifiedEvent = "FremodifiedEvent";
    /**
     * fre
     */
    public static final String Cmdfrepowerquerycmd = "LST CELL:; LST PDSCHCFG:; LST CELLDLPCPDSCHPA:;";

    /**
     * ip
     */
    public static final String Cmdipconfigquery = "LST IPINTERFACE:;";

    /**
     * ROUTE
     */
    public static final String Cmdrouteconfigquery = "LST ROUTE:;";


    /**
     * route_cn
     */
    public static final String Routeid_cn = PicApplication.getPicContext().getResources().getString(R.string
            .str_routeid);
    public static final String Restiipaddr_cn = PicApplication.getPicContext().getResources().getString(R.string
            .str_destiipaddr);
    public static final String Destimask_cn = PicApplication.getPicContext().getResources().getString(R.string
            .str_destimask);
    public static final String Nexthopip_cn = PicApplication.getPicContext().getResources().getString(R.string
            .str_nexthopip);

    /**
     * frequ_cn
     */
    public static final String Localid_cn = PicApplication.getPicContext().getResources().getString(R.string
            .str_localid);
    public static final String Dlearfcn_cn = PicApplication.getPicContext().getResources().getString(R.string
            .str_dlearfcn);
    public static final String Dlbandwidth_cn = PicApplication.getPicContext().getResources().getString(R.string
            .str_dlbandwidth);
    public static final String Pa_cn = PicApplication.getPicContext().getResources().getString(R.string.str_pa);
    public static final String E_rs_cn = PicApplication.getPicContext().getResources().getString(R.string.str_e_rs);


}
