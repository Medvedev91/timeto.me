import SwiftUI

struct ReadmeImagesPreview: View {
    
    let images: [String]
    
    @State private var isFullScreenPresented = false
    
    var body: some View {
        
        ScrollView(.horizontal, showsIndicators: false) {
            
            HStack {
                
                ForEach(images, id: \.self) { item in
                    
                    Button(
                        action: {
                            isFullScreenPresented = true
                        },
                        label: {
                            Image(item)
                                .resizable()
                                .aspectRatio(contentMode: .fit)
                                .cornerRadius(16)
                                .shadow(color: .primary, radius: onePx)
                                .frame(height: 350)
                                .padding(.trailing, 8)
                                .padding(.vertical, 4) // Paddings for shadow radius
                        }
                    )
                }
            }
            .scrollTargetLayout()
        }
        .contentMargins(.leading, 16, for: .scrollContent)
        .contentMargins(.trailing, 8, for: .scrollContent)
        .scrollTargetBehavior(.viewAligned)
        .padding(.top, 20)
        .fullScreenCover(isPresented: $isFullScreenPresented) {
            ReadmeImagesFullScreen(
                images: images
            )
        }
    }
}
