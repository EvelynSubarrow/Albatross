package moe.evelyn.albatross;

import moe.evelyn.albatross.utils.AnnotationConfig;

public class Config extends AnnotationConfig
{
    @config public boolean updateCheck = true;
    @config(settable=false) public boolean verifyUpdateCertificate = false;

    @Override
    public String getPrefix() {
        return "/commandspy config";
    }
}
