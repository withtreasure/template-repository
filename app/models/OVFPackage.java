package models;

import java.io.UnsupportedEncodingException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.net.URLEncoder;

import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.xml.ws.WebServiceException;

import org.apache.commons.io.FilenameUtils;
import org.joda.time.DateTime;
import org.joda.time.format.DateTimeFormatter;
import org.joda.time.format.DateTimeFormatterBuilder;

import play.data.parsing.UrlEncodedParser;
import play.data.validation.MaxSize;
import play.data.validation.Required;
import play.data.validation.Unique;
import play.db.jpa.Model;
import play.libs.Codec;
import play.mvc.Router;

@Entity
public class OVFPackage extends Model
{
	 // DEFAULTS
    private final static Integer CPU_DEFAULT = 1;

    private final static Long RAM_DEFAULT = 512l;

    private final static Long HD_DEFAULT = 2l;
    
    final static DateTimeFormatter TIME_FORMATTER = new DateTimeFormatterBuilder()
		.appendMonthOfYearShortText()
		.appendDayOfMonth(2).appendLiteral('_')//
		.appendHourOfDay(2).appendLiteral("-").appendMinuteOfHour(2).toFormatter();
    
    @Required
    @MaxSize(256)
    public String name;

    @Required
    @Enumerated(EnumType.STRING)
    public DiskFormatType diskFileFormat;

    @MaxSize(1024)
    public String description;
    
    @Required
    @Unique
    @MaxSize(512)
    public String diskFilePath;

    @Required
    public Long diskFileSize;

    public Integer cpu;

    public Long ram;

    public Long hd;

    public Long hdInBytes;

    @Enumerated(EnumType.STRING)
    public MemorySizeUnitType ramSizeUnit;

    @Enumerated(EnumType.STRING)
    public MemorySizeUnitType hdSizeUnit;

    @MaxSize(256)
    public String iconPath;

    @MaxSize(256)
    public String categoryName;

    @MaxSize(256)
    public String userMail;

    @Enumerated(EnumType.STRING)
    public EthernetDriver ethernetDriver;

    public Integer templateVersion;
    
    // more-info

    public String user;
    
    public String password;
    
    @Enumerated(EnumType.STRING)    
    public OSType osType;
    
    public String osVersion;
    
    @Enumerated(EnumType.STRING)
    public DiskController diskController;
    
    /**
     * 
     * */
    @Required
    @Unique
    @MaxSize(256)
    public String nameUrl;
    

    public void setTemplateVersion(final Integer templateVersion)
    {
        this.templateVersion = templateVersion;
    }

    public Integer getTemplateVersion()
    {
        return templateVersion != null ? templateVersion : 0;    
    }

    public Integer getCpu()
    {
        return cpu != null ? cpu : CPU_DEFAULT;
    }

    public Long getHd()
    {
        return hd != null ? hd : HD_DEFAULT;
    }

    public String getCategoryName()
    {
        return categoryName != null ? categoryName : "Others";
    }

    public Long getHdInBytes()
    {
        return getHdSizeUnit() == MemorySizeUnitType.BYTE ? getHd() : hdInBytes(getHd().doubleValue(), getHdSizeUnit());
    }
    public String getUser()
    {
    	return user != null ? user : "user";
    }
    
    public String getPassword()
    {
    	return password != null ? password: "password";
    }
    
    public DiskController getDiskController()
    {
    	return diskController != null ? diskController : DiskController.SCSI;
    }
    
    public OSType getOsType()
    {
    	return osType != null ? osType : OSType.UNRECOGNIZED;
    }
    
    public String getOsVersion()
    {
    	return osVersion != null ? osVersion : "";
    }
    
    public Long getRam()
    {
        return ram != null ? ram : RAM_DEFAULT;
    }

    public MemorySizeUnitType getRamSizeUnit()
    {
        return ramSizeUnit != null ? ramSizeUnit : MemorySizeUnitType.MB;
    }

    public MemorySizeUnitType getHdSizeUnit()
    {
        return hdSizeUnit != null ? hdSizeUnit : MemorySizeUnitType.GB;
    }

    public String getIconPath()
    {

        return iconPath != null ? iconPath : Router.getFullUrl("OVFPackages.list")
            + "public/icons/q.png";
    }

    public DiskFormatType getDiskFileFormat()
    {
        return diskFileFormat != null ? diskFileFormat : DiskFormatType.VMDK_STREAM_OPTIMIZED;
    }

    public EthernetDriver getEthernetDriver()
    {
        return ethernetDriver != null ? ethernetDriver : EthernetDriver.E1000;
    }

    public String getNameUrl()
    {
        return nameUrl != null ? nameUrl : name;
    }

    public void setEthernetDriver(final EthernetDriver ethernetDriver)
    {
        this.ethernetDriver = ethernetDriver;
    }
    
    public static Long hdInBytes(final Double hd, final MemorySizeUnitType units)
    {
        Double hdB = hd;

        switch (units)
        {
            case TB:
                hdB = hdB * 1024;
            case GB:
                hdB = hdB * 1024;
            case MB:
                hdB = hdB * 1024;
            case KB:
                hdB = hdB * 1024;
            default:
                break;
        }

        return hdB.longValue();

    }
    
    public void unicNameOrAppendTimestamp() 
    {
        // remove extension from default name (file name)
        try
        {
            nameUrl =  URLEncoder.encode(FilenameUtils.getBaseName(name), "UTF-8");
        }
        catch (UnsupportedEncodingException e)
        {
            throw new WebServiceException(e);
        }

        // check name is unique or append time-stamp
        if (find("name", nameUrl).first() != null)
        {
            nameUrl = nameUrl +"_"+new DateTime().toString(TIME_FORMATTER);
        }
    }
    
    public boolean isDiskUrl()
    {
        return diskFilePath!= null && diskFilePath.startsWith("http://");
    }

    public void computeSimpleUnits(final long hdInBytes)
    {
        this.hd = hdInBytes / 1048576;
        this.hdSizeUnit = MemorySizeUnitType.MB;
    }
    
    public String gravatarCreator()
    {
        return userMail == null || userMail.equalsIgnoreCase("not authenticated")?"http://www.gravatar.com/avatar/default.jpeg"://
            String.format("http://www.gravatar.com/avatar/%s.jpeg", Codec.hexMD5(userMail));
    }
    
    
    public String getSimpleFileName() throws UnsupportedEncodingException
    {        
		return diskFilePath == null ? "" : 
		    URLEncoder.encode(FilenameUtils.getName(diskFilePath), "UTF-8")
				.replace("+", "%20"); // application/x-www-form-urlencoded
    }

    public OVFPackage()
    {

    }

	public OVFPackage(final String name, final String description,
			final DiskFormatType diskFileFormat, final String diskFilePath,
			final Long diskFileSize, final Integer cpu, final Long ram,
			final Long hd, final MemorySizeUnitType ramSizeUnit,
			final MemorySizeUnitType hdSizeUnit, final String iconPath,
			final String categoryName, final String userMail,
			final String user, final String password,
			final DiskController diskController, final OSType osType,
			final String osVersion)
	{
		super();

        this.user = user;
        this.password = password;
        this.diskController = diskController;
        this.osType = osType;
        this.osVersion = osVersion;
        this.userMail = userMail;
        this.name = name;
        this.nameUrl = name;
        this.diskFileFormat = diskFileFormat;
        this.description = description;
        this.diskFilePath = diskFilePath;
        this.diskFileSize = diskFileSize;
        this.cpu = cpu;
        this.ram = ram;
        this.hd = hd;
        this.ramSizeUnit = ramSizeUnit;
        this.hdSizeUnit = hdSizeUnit;
        this.iconPath = iconPath;
        this.categoryName = categoryName;
    }

    @Override
    public String toString()
    {
        return String.format("name:%s, format:%s", name, getDiskFileFormat().name());
    }

    public enum MemorySizeUnitType
    {
        BYTE, KB, MB, GB, TB;

        @Override
        public String toString()
        {
            return this.name();
        }
    }

    public enum EthernetDriver
    {
        /* 0 */ E1000,
        /* 1 */ PCNet32, 
        /* 2 */ VMXNET3
    }

    public enum DiskController
    {
    	/* 0 */ IDE,
    	/* 1 */ SCSI
    }
    
    // https://raw.github.com/jclouds/jclouds/master/compute/src/main/java/org/jclouds/cim/OSType.java
    public enum OSType
    {
    	OTHER(1, "Other", false),
		MACOS(2, "MACOS", false),
		SOLARIS(29, "Solaris", false),
		LINUX(36, "LINUX", false),
		FREEBSD(42, "FreeBSD", false),
		NETBSD(43, "NetBSD", false),
		OPENBSD(65, "OpenBSD", false),
		NOT_APPLICABLE(66, "Not Applicable", false),
		WINDOWS_SERVER_2003(69, "Microsoft Windows Server 2003", false),
		WINDOWS_SERVER_2003_64(70, "Microsoft Windows Server 2003 64-Bit", true),
		WINDOWS_SERVER_2008(76, "Microsoft Windows Server 2008", false),
		WINDOWS_SERVER_2008_64(77, "Microsoft Windows Server 2008 64-Bit", true),
		FREEBSD_64(78, "FreeBSD 64-Bit", true),
		RHEL(79, "RedHat Enterprise Linux", false),
		RHEL_64(80, "RedHat Enterprise Linux 64-Bit", true),
		SOLARIS_64(81, "Solaris 64-Bit", true),
		SUSE(82, "SUSE", false),
		SUSE_64(83, "SUSE 64-Bit", true),
		SLES(84, "SLES", false),
		SLES_64(85, "SLES 64-Bit", true),
		NOVELL_OES(86, "Novell OES", true),
		MANDRIVA(89, "Mandriva", false),
		MANDRIVA_64(90, "Mandriva 64-Bit", true),
		TURBOLINUX(91, "TurboLinux", false),
		TURBOLINUX_64(92, "TurboLinux 64-Bit", true),
		UBUNTU(93, "Ubuntu", false),
		UBUNTU_64(94, "Ubuntu 64-Bit", true),
		DEBIAN(95, "Debian", false),
		DEBIAN_64(96, "Debian 64-Bit", false),
		LINUX_2_4(97, "Linux 2.4.x", false),
		LINUX_2_4_64(98, "Linux 2.4.x 64-Bit", true),
		LINUX_2_6(99, "Linux 2.6.x", false),
		LINUX_2_6_64(100, "Linux 2.6.x 64-Bit", true),
		LINUX_64(101, "Linux 64-Bit", true),
		OTHER_64(102, "Other 64-Bit", true),
		WINDOWS_SERVER_2008_R2(103, "Microsoft Windows Server 2008 R2", true),
		ESXI(104, "VMware ESXi", true),
		WINDOWS_7(105, "Microsoft Windows 7", false),
		CENTOS(106, "CentOS 32-bit", false),
		CENTOS_64(107, "CentOS 64-bit", true),
		ORACLE_ENTERPRISE_LINUX(108, "Oracle Enterprise Linux 32-bit", false),
		ORACLE_ENTERPRISE_LINUX_64(109, "Oracle Enterprise Linux 64-bit", true),
		ECOMSTATION_32(109, "eComStation 32-bitx", false), 
		UNRECOGNIZED(Integer.MAX_VALUE, "UNRECOGNIZED", true);
    
    	final int code;
    	final String description;
    	final boolean is64b;
    	
    	OSType(int code, String description, boolean is64b)
    	{
    		this.code = code;
    		this.description = description;
    		this.is64b = is64b;
    	}
    	
		public int getCode()
		{
			return code;
		}

		public String getDescription()
		{
			return description;
		}

		public boolean is64Bit()
		{
			return is64b;
		}
    }
    
    public enum DiskFormatType
    {
        /* 0 */UNKNOWN("http://unknown"),
        /* 1 */RAW("http://raw"),
        /* 2 */INCOMPATIBLE("http://incompatible"),
        /* 3 */VMDK_STREAM_OPTIMIZED("http://www.vmware.com/technical-resources/interfaces/vmdk_access.html#streamOptimized"),
        /* 4 */VMDK_FLAT("http://www.vmware.com/technical-resources/interfaces/vmdk_access.html#monolithic_flat"),
        /* 5 */VMDK_SPARSE("http://www.vmware.com/technical-resources/interfaces/vmdk_access.html#monolithic_sparse"),
        /* 6 */VHD_FLAT("http://technet.microsoft.com/en-us/virtualserver/bb676673.aspx#monolithic_flat"),
        /* 7 */VHD_SPARSE("http://technet.microsoft.com/en-us/virtualserver/bb676673.aspx#monolithic_sparse"),
        /* 8 */VDI_FLAT("http://forums.virtualbox.org/viewtopic.php?t=8046#monolithic_flat"),
        /* 9 */VDI_SPARSE("http://forums.virtualbox.org/viewtopic.php?t=8046#monolithic_sparse"),
        /* 10 */QCOW2_FLAT("http://people.gnome.org/~markmc/qcow-image-format.html#monolithic_flat"),
        /* 11 */QCOW2_SPARSE("http://people.gnome.org/~markmc/qcow-image-format.html#monolithic_sparse");

        private final String url;

        private DiskFormatType(final String url)
        {
            this.url = url;
        }

        public String url()
        {
            return url;
        }

        @Override
        public String toString()
        {
            return this.name();
        }
    }    
}
