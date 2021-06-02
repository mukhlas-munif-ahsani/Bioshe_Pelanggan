package com.munifahsan.biosheapp.ui.pageHome;

import com.google.firebase.firestore.DocumentId;

public class PromoModel {
    @DocumentId
    String id;
    String nImage;

    public PromoModel(String id, String nImage) {
        this.id = id;
        this.nImage = nImage;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getnImage() {
        return nImage;
    }

    public void setnImage(String nImage) {
        this.nImage = nImage;
    }
}
