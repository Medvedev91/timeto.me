import SwiftUI

private let pTextLineHeight = 3.2

struct Readme2FullScreen: View {
    
    @Environment(\.dismiss) private var dismiss
    
    var body: some View {
        ScrollView(showsIndicators: false) {
            
            TextView(text: Text("The main feature of this app is ") + Text("goals").underline() + Text(":"))
            
            ScreenshotView(image: "readme_goals")
            
            TextView(text: Text("Tap a goal to start a timer with the remaining time for that goal:"))
            
            ScreenshotView(image: "readme_timer")
            
            TextView(text: Text("Timer is running ") +
                     Text("all the time").underline() +
                     Text(", 24/7, even for sleep or breakfast. ") +
                     Text("There is no stop option").underline() +
                     Text("! To stop the current goal, you have to start the next one.")
            )
            
            TextView(text: Text("You can add a checklist for goals. Useful for morning/evening routines, work, exercises, etc. Like this:"))
            
            ScreenshotView(image: "readme_checklist")
            
            TextView(text: Text("This way I control my time and don't forget anything."))
            
            TextView(text: Text("Try to adapt it to your life."))
            
            TextView(text: Text("Best regards,\nIvan"))
        }
        .navigationTitle("How to Use the App")
        .toolbar {
            ToolbarItem(placement: .primaryAction) {
                Button("Done") {
                    dismiss()
                }
                .fontWeight(.semibold)
            }
        }
    }
}

private struct TextView: View {
    
    let text: Text
    
    var body: some View {
        text
            .textAlign(.leading)
            .foregroundColor(.primary)
            .padding(.horizontal, H_PADDING)
            .lineSpacing(pTextLineHeight)
            .padding(.vertical, 4)
    }
}

private struct ScreenshotView: View {
    
    let image: String
    
    var body: some View {
        Image(image)
            .resizable()
            .aspectRatio(contentMode: .fit)
            .cornerRadius(16)
            .shadow(color: .primary, radius: onePx)
            .padding(.horizontal, 8)
            .padding(.vertical, 4) // Paddings for shadow radius
    }
}
