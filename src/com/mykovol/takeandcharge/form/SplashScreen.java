package com.mykovol.takeandcharge.form;

import com.codename1.components.ScaleImageLabel;
import com.codename1.ui.*;
import com.codename1.ui.animations.CommonTransitions;
import com.codename1.ui.layouts.BorderLayout;
import com.codename1.ui.layouts.BoxLayout;
import com.codename1.ui.layouts.LayeredLayout;
import com.codename1.ui.plaf.Style;
import com.codename1.ui.util.Resources;
import com.mykovol.takeandcharge.service.RentSocketService;

import static com.codename1.ui.CN.callSerially;
import static com.codename1.ui.layouts.BorderLayout.CENTER_BEHAVIOR_CENTER_ABSOLUTE;

public class SplashScreen extends Form {
    private final ScaleImageLabel logoImage1 = new ScaleImageLabel(Resources.getGlobalResources().getImage("splash-logo-part1.png"));
    private final Container animatedLogoImage1Container = BorderLayout.center(logoImage1);
    private final Label logoImage2 = new Label(Resources.getGlobalResources().getImage("splash-logo-part2.png"));
    private final Container animatedLogoImage2Container = BorderLayout.centerAbsolute(logoImage2);

    private final Label logoTitle = new Label("Take&Charge", "SplashTitle");
    private final Label logoTitlePlaceHolder = new Label(" ", "SplashTitle");
    private final Label logoSubTitle = new Label("powerbank sharing solution", "SplashSubTitle");
    private final Label logoSubTitlePlaceholder = new Label(" ", "SplashSubTitle");

    private final Container logoImageContainer = LayeredLayout.encloseIn(
            new Label(logoImage1.getIcon(), "TextAlignCenter"),
            new Label(logoImage2.getIcon(), "TextAlignCenter"));
    private final Container mainContainer = BoxLayout.encloseY(logoImageContainer);

    public SplashScreen() {
        super(new LayeredLayout());
//        getContentPane().setUIID("Container");
//        getToolbar().setUIID("Container");
        setToolbar(new Toolbar(true));
        getToolbar().hideToolbar();
        setTransitionOutAnimator(CommonTransitions.createEmpty());

//        MorphTransition morph = MorphTransition.create(400);
//        setTransitionOutAnimator(morph);

        logoImage1.setBackgroundType(Style.BACKGROUND_IMAGE_SCALED_FILL);
        add(animatedLogoImage1Container);
        add(animatedLogoImage2Container);

        Component.setSameHeight(logoTitle, logoSubTitle);
        Component.setSameWidth(logoTitle, logoSubTitle, mainContainer);
    }

    @Override
    public void show() {
        super.show();

        callSerially(() -> {
            animateLogoIconAppearance();


            callSerially(() -> {
                animateTitle();
                callSerially(RentSocketService::get);
                animateSubTitle();
//                mainContainer.add(BoxLayout.encloseXCenter(new InfiniteProgress()));

                AnimationManager a = getAnimationManager();
                if (a != null) {
                    a.flushAnimation(new Runnable() {
                        @Override
                        public void run() {
                            MainForm.get().show();
                        }
                    });
                }
            });


//                MainForm mainForm = MainForm.get();
//                callSerially(() -> {
////                    animateLogoFlayAway();
//                    revalidateWithAnimationSafety();
//                    mainForm.show();
//                });
        });
    }

    private void animateSubTitle() {
        mainContainer.add(logoSubTitlePlaceholder);
        mainContainer.getParent().animateLayoutAndWait(300);
        mainContainer.replaceAndWait(logoSubTitlePlaceholder, logoSubTitle, CommonTransitions.createFade(100));

    }

    private void animateTitle() {
        mainContainer.add(logoTitlePlaceHolder);
        mainContainer.getParent().animateLayoutAndWait(300);
        mainContainer.replaceAndWait(logoTitlePlaceHolder, logoTitle, CommonTransitions.createFade(100));
    }


    private void animateLogoFlayAway() {
        logoSubTitle.setY(getHeight());
        logoTitle.setY(getHeight());
        logoImageContainer.setY(getHeight());
        mainContainer.setHeight(getHeight());
        mainContainer.animateUnlayoutAndWait(450, 20);
//        MorphTransition morph = MorphTransition.create(300);
//        setTransitionOutAnimator(morph);
//        setTransitionOutAnimator(CommonTransitions.createEmpty());
    }

    private void animateLogoIconAppearance() {
        ((BorderLayout) animatedLogoImage1Container.getLayout()).setCenterBehavior(CENTER_BEHAVIOR_CENTER_ABSOLUTE);
        animatedLogoImage1Container.setShouldCalcPreferredSize(true);
        animatedLogoImage1Container.animateLayoutAndWait(300);

        logoImage2.remove();
        logoImage1.remove();
        animatedLogoImage2Container.remove();

        animatedLogoImage1Container.add(CENTER, mainContainer);
        revalidateWithAnimationSafety();
    }
}
