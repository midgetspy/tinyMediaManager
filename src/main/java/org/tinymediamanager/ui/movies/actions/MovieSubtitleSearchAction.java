/*
 * Copyright 2012 - 2017 Manuel Laggner
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.tinymediamanager.ui.movies.actions;

import java.awt.event.ActionEvent;
import java.util.ArrayList;
import java.util.List;
import java.util.ResourceBundle;

import javax.swing.AbstractAction;

import org.tinymediamanager.core.MediaFileType;
import org.tinymediamanager.core.entities.MediaFile;
import org.tinymediamanager.core.movie.entities.Movie;
import org.tinymediamanager.ui.IconManager;
import org.tinymediamanager.ui.UTF8Control;
import org.tinymediamanager.ui.movies.MovieUIModule;
import org.tinymediamanager.ui.movies.dialogs.MovieSubtitleChooserDialog;

/**
 * The MovieSubtitleSearchAction - search for subtitles for all selected movies
 * 
 * @author Manuel Laggner
 */
public class MovieSubtitleSearchAction extends AbstractAction {
  private static final long           serialVersionUID = -6006932119900795735L;
  private static final ResourceBundle BUNDLE           = ResourceBundle.getBundle("messages", new UTF8Control()); //$NON-NLS-1$

  public MovieSubtitleSearchAction() {
    putValue(NAME, BUNDLE.getString("movie.search.subtitle")); //$NON-NLS-1$
    putValue(SMALL_ICON, IconManager.SUBTITLE);
    putValue(LARGE_ICON_KEY, IconManager.SUBTITLE);
    putValue(SHORT_DESCRIPTION, BUNDLE.getString("movie.search.subtitle")); //$NON-NLS-1$
  }

  @Override
  public void actionPerformed(ActionEvent e) {
    List<Movie> selectedMovies = new ArrayList<>(MovieUIModule.getInstance().getSelectionModel().getSelectedMovies());

    for (Movie movie : selectedMovies) {
      // no subtitle download for discs
      if (movie.isDisc()) {
        continue;
      }

      // show the dialog for every movie video file (multi part video files problem)
      for (MediaFile mediaFile : movie.getMediaFiles(MediaFileType.VIDEO)) {
        MovieSubtitleChooserDialog dialogMovieSubtitleChooser = new MovieSubtitleChooserDialog(movie, mediaFile, selectedMovies.size() > 1);
        if (!dialogMovieSubtitleChooser.showDialog()) {
          return;
        }
      }
    }
  }
}
