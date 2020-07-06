package ru.ancevt.webparsers.headhunter.ds;

import ru.ancevt.util.string.ToStringBuilder;

/**
 *
 * @author ancevt
 */
public class ContactInfo {

    private String[] phones;
    private String fullName;
    private String email;
    private String comment;

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String[] getPhones() {
        return phones;
    }

    public void setPhones(String[] phones) {
        this.phones = phones;
    }

    public String getFullName() {
        return fullName;
    }

    public void setFullName(String fullName) {
        this.fullName = fullName;
    }

    public String getComment() {
        return comment;
    }

    public void setComment(String comment) {
        this.comment = comment;
    }
    
    public String getPhonesString() {
        if(phones == null) return null;
        
        final StringBuilder sbPhones = new StringBuilder();
        for (int i = 0; i < phones.length; i++) {
            final String phone = phones[i];
            sbPhones.append(phone);

            if (i != phones.length - 1) {
                sbPhones.append("; ");
            }
        }
        
        return sbPhones.toString();
    }

    @Override
    public String toString() {
        final StringBuilder sbPhones = new StringBuilder("[");
        for (int i = 0; i < phones.length; i++) {
            final String phone = phones[i];
            sbPhones.append(phone);

            if (i != phones.length - 1) {
                sbPhones.append("; ");
            }
        }
        sbPhones.append(']');

        return new ToStringBuilder(this)
                .append("fullName", fullName)
                .append("comment", comment)
                .append("email", email)
                .append("phones", sbPhones)
                .build();
    }

}

/*"contactInfo": {
 "phones": {"phones": [{
 "country": "7",
 "number": "2892725",
 "city": "383",
 "comment": null
 }]},
 "fio": "Маринникова Майя Константиновна",
 "email": "info@plp-nso.ru"
 },*/
