// Generated by view binder compiler. Do not edit!
package de.blinkt.openvpn.databinding;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.ImageView;
import android.widget.ScrollView;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.viewbinding.ViewBinding;
import de.blinkt.openvpn.R;
import java.lang.NullPointerException;
import java.lang.Override;
import java.lang.String;

public final class ApiConfirmBinding implements ViewBinding {
  @NonNull
  private final ScrollView rootView;

  @NonNull
  public final CheckBox check;

  @NonNull
  public final ImageView icon;

  @NonNull
  public final TextView prompt;

  @NonNull
  public final TextView warning;

  private ApiConfirmBinding(@NonNull ScrollView rootView, @NonNull CheckBox check,
      @NonNull ImageView icon, @NonNull TextView prompt, @NonNull TextView warning) {
    this.rootView = rootView;
    this.check = check;
    this.icon = icon;
    this.prompt = prompt;
    this.warning = warning;
  }

  @Override
  @NonNull
  public ScrollView getRoot() {
    return rootView;
  }

  @NonNull
  public static ApiConfirmBinding inflate(@NonNull LayoutInflater inflater) {
    return inflate(inflater, null, false);
  }

  @NonNull
  public static ApiConfirmBinding inflate(@NonNull LayoutInflater inflater,
      @Nullable ViewGroup parent, boolean attachToParent) {
    View root = inflater.inflate(R.layout.api_confirm, parent, false);
    if (attachToParent) {
      parent.addView(root);
    }
    return bind(root);
  }

  @NonNull
  public static ApiConfirmBinding bind(@NonNull View rootView) {
    // The body of this method is generated in a way you would not otherwise write.
    // This is done to optimize the compiled bytecode for size and performance.
    int id;
    missingId: {
      id = R.id.check;
      CheckBox check = rootView.findViewById(id);
      if (check == null) {
        break missingId;
      }

      id = R.id.icon;
      ImageView icon = rootView.findViewById(id);
      if (icon == null) {
        break missingId;
      }

      id = R.id.prompt;
      TextView prompt = rootView.findViewById(id);
      if (prompt == null) {
        break missingId;
      }

      id = R.id.warning;
      TextView warning = rootView.findViewById(id);
      if (warning == null) {
        break missingId;
      }

      return new ApiConfirmBinding((ScrollView) rootView, check, icon, prompt, warning);
    }
    String missingId = rootView.getResources().getResourceName(id);
    throw new NullPointerException("Missing required view with ID: ".concat(missingId));
  }
}
