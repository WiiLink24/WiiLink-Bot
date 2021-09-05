package com.wiilink24.bot.commands.misc;

import com.jagrosh.jdautilities.command.Command;
import com.jagrosh.jdautilities.command.CommandEvent;
import com.google.cloud.vision.v1.AnnotateImageRequest;
import com.google.cloud.vision.v1.AnnotateImageResponse;
import com.google.cloud.vision.v1.BatchAnnotateImagesResponse;
import com.google.cloud.vision.v1.Feature;
import com.google.cloud.vision.v1.Image;
import com.google.cloud.vision.v1.ImageAnnotatorClient;
import com.google.cloud.vision.v1.ImageSource;
import com.wiilink24.bot.commands.Categories;
import io.sentry.Sentry;
import net.dv8tion.jda.api.EmbedBuilder;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * Command that pulls text from images.
 * The main use is for WiiLink Translators.
 *
 * @author Sketch
 */

public class OCR extends Command {
    public OCR() {
        this.name = "ocr";
        this.arguments = "[url]";
        this.category = Categories.MISC;
        this.help = "Grabs text from the specified image url.";
    }

    @Override
    protected void execute(CommandEvent event) {
        event.async(() -> {
            try {
                getOCR(event);
            } catch (IOException e) {
                event.replyError("An error has occurred: " + e.getMessage());
                Sentry.captureException(e);
            }
        });
    }

    private void getOCR(CommandEvent event) throws IOException {
        List<AnnotateImageRequest> requests = new ArrayList<>();

        ImageSource imgSource = ImageSource.newBuilder().setImageUri(event.getArgs()).build();
        Image img = Image.newBuilder().setSource(imgSource).build();
        Feature feat = Feature.newBuilder().setType(Feature.Type.TEXT_DETECTION).build();
        AnnotateImageRequest request =
                AnnotateImageRequest.newBuilder().addFeatures(feat).setImage(img).build();
        requests.add(request);

        try (ImageAnnotatorClient client = ImageAnnotatorClient.create()) {
            BatchAnnotateImagesResponse response = client.batchAnnotateImages(requests);
            List<AnnotateImageResponse> responses = response.getResponsesList();

            for (AnnotateImageResponse res : responses) {
                if (res.hasError()) {
                    System.out.format("Error: %s%n", res.getError().getMessage());
                    return;
                }

                // The text we need will always be in the first index.
                EmbedBuilder embed = new EmbedBuilder()
                        .setTitle("Optical Character Recognition")
                        .setColor(0xADD8E6)
                        .setDescription(String.format("```%s```",res.getTextAnnotationsList().get(0).getDescription()));

                client.close();
                event.reply(embed.build());
            }


        }
    }
}
